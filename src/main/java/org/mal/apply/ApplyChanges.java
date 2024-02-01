package org.mal.apply;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mal.Configurations;
import org.mal.FileIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.mal.FileIO.*;

public class ApplyChanges {
    public static String replaceContent(String originalString, int start, int end, String newContent) {
        StringBuilder sb = new StringBuilder(originalString);
        return sb.delete(start, end).insert(start, newContent).toString();
    }

    public void processImprovements() throws Exception {
        File file = new File("selected_repos_updated.txt");
        try {
            List<String> lines = FileUtils.readLines(file, "UTF-8");
            for (String projectName : lines) {
                incoperateChangesAndCompile(projectName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void incoperateChangesAndCompile(String projectName) throws Exception {
        Path projectPath = Paths.get(Configurations.IMPROVEMENTS, projectName);
        if (!Files.exists(projectPath) || !Files.isDirectory(projectPath)) {
            throw new FileNotFoundException("Improvements cannot be found for " + projectName);
        }
        int numberOfUnsuccessfulCompiles = 0;
        int totalParsableImprovementFiles = 0;
        int totalUnParsableImprovementFiles = 0;
        List<Path> jsons = FileIO.getAllFilesInDirectory(Path.of(Configurations.IMPROVEMENTS + "/" + projectName + "/"), "");
        for (Path json : jsons) {
            System.out.println(json);
            if (json.getFileName().toString().matches("\\d+") && !validJSON(json)) {
                totalUnParsableImprovementFiles += 1;
                continue;
            }
            totalParsableImprovementFiles += 1;
            if (json.getFileName().toString().matches("\\d+")) {
                if(prepareProjectForImprovements(projectName)){
                    Improvement improvement = getImprovement(json);
                    Path path = Path.of(Configurations.PROJECT_REPOSITORY + improvement.getFilePath());
                    Path backupPath = path.resolveSibling(path.getFileName() + ".bak");
                    backupFile(path, backupPath);
                    String oldFileContent = FileIO.readStringFromFile(Configurations.PROJECT_REPOSITORY +
                            improvement.getFilePath());
                    String newFileContent = replaceContent(oldFileContent, improvement.getStart(), improvement.getStop(), improvement.getImprovedCode());
                    // Apply improvement
                    modifyFileContent(path, newFileContent);  // add the changed method
                    MavenCommandResult result = compileProject(projectName);
                    if (result.getExitCode() != 0) {
                        FileIO.writeLinesToFile(Configurations.COMPILE_ERRORS + projectName + "/" + json.getFileName() + ".txt", result.getOutput());
                        numberOfUnsuccessfulCompiles += 1;

                    }
                    restoreFile(backupPath, path);  // revert the change
                }
                System.out.println("Total parsable improvement files: " + totalParsableImprovementFiles);
                System.out.println("Total unparsable improvement files: " + totalUnParsableImprovementFiles);
                System.out.println("Total unsuccessful compiles: " + numberOfUnsuccessfulCompiles);
            }
        }

    }

    private static MavenCommandResult compileProject(String projectName) {
        MavenCommandRunner.mvnClean(new File(Configurations.PROJECT_REPOSITORY + projectName + "/"));
        return MavenCommandRunner.mvnCompile(new File(Configurations.PROJECT_REPOSITORY + projectName + "/"));
    }

    private boolean prepareProjectForImprovements(String projectName) throws MavenOperationException {
        File projectDirectory = new File(Configurations.PROJECT_REPOSITORY + projectName + "/");

        // Ensure the project directory exists and is a directory
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            throw new MavenOperationException("Project directory does not exist or is not a directory: " + projectDirectory.getPath());
        }

        // Attempt to clean the project
        MavenCommandResult cleanResult = MavenCommandRunner.mvnClean(projectDirectory);
        if (cleanResult.getExitCode() != 0) {
            // Including more specific details from Maven output can be helpful for debugging
            throw new MavenOperationException("Failed to clean the project: " + projectName + ". Error: " + cleanResult.getOutput());
        }

        // Attempt to compile the project
        MavenCommandResult compileResult = MavenCommandRunner.mvnCompile(projectDirectory);
        if (compileResult.getExitCode() != 0) {
            throw new MavenOperationException("Failed to compile the project: " + projectName + ". Error: " + compileResult.getOutput());
        }

        return true; // Project is cleaned and compiled successfully
    }

    private boolean validJSON(Path json) throws IOException {
        JSONObject object = FileIO.readJSONObjectFromFile(json);
        JSONArray ims = object.getJSONArray("Method_Improvements");
        for (Object o : ims) {
            JSONObject im = (JSONObject) o;
            if (im.keySet().contains("error")) {
                if (im.getString("error").equals("An unexpected error occurred")) {
                    return false;
                }
            }
        }
        return true;
    }

    private Improvement getImprovement(Path json) throws IOException {
        JSONObject object = FileIO.readJSONObjectFromFile(json);
        String filePath = (String) object.get("File_Path");
        Integer startC = (Integer) object.get("Start");
        Integer startE = (Integer) object.get("Stop");
        JSONArray arrayImv = (JSONArray) object.get("Method_Improvements");
        StringBuilder improvedCode = new StringBuilder();
        for (Object o : arrayImv) {
            JSONObject ob = (JSONObject) o;
            improvedCode.append(ob.getString("Final code"));
        }
        return new Improvement(improvedCode.toString(), startC, startE, filePath);
    }

    private void improveFile(String filePath, String newContent) {
        Path path = Path.of(filePath);
        Path backupPath = path.resolveSibling(path.getFileName() + ".bak");
    }

    // Custom exception class for Maven operation failures
    class MavenOperationException extends Exception {
        public MavenOperationException(String message) {
            super(message);
        }
    }
}
