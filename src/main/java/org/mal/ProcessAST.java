package org.mal;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
public class ProcessAST {
    static Logger logger = Logger.getLogger(ProcessAST.class);

    private static List<MethodDeclaration> processJavaFile(Path javaFilePath) {
        System.out.println("Processing: " + javaFilePath);
        ASTNode node = JavaASTUtil.parseSource(Objects.requireNonNull(FileIO.readStringFromFile(javaFilePath.toString())));
        CompilationUnit cUnit = (CompilationUnit)node;
        MethodVisitor visitor = new MethodVisitor();
        cUnit.accept(visitor);
        return visitor.methods.stream().map(x -> new MethodDeclaration(x, x.getName().getFullyQualifiedName(),
                x.getStartPosition(), x.getLength() + x.getStartPosition(),
                Paths.get(Configurations.PROJECT_REPOSITORY).relativize(javaFilePath).toString())).toList();
    }

    public boolean processProjects(){
        File file = new File("selected_repos_updated.txt");
        try {
            List<String> lines = FileUtils.readLines(file, "UTF-8");
            for (String projectName : lines) {
                MyGit.cloneIfNotExits(projectName,Configurations.PROJECT_REPOSITORY);
                analyzeProject(new File(Configurations.PROJECT_REPOSITORY+projectName).toPath(),projectName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * analyse a java project file by file, identify methods, sort methods depending on
     * the length, write improvements for each method in a file
     * @param project
     * @param projectName
     * @throws IOException
     */
    private void analyzeProject(Path project, String projectName) throws IOException {
        ArrayList<String> folders = new ArrayList<>();
        folders.add("src");
        List<MethodDeclaration> allMethods = new ArrayList<>();
        for (String folder : folders) {
            project = Path.of(project.toString(),folder);
            if (project.toFile().isDirectory()){
                try (Stream<Path> paths = Files.walk(project)) {
                    allMethods.addAll(  paths.filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".java"))
                            .flatMap(x -> ProcessAST.processJavaFile(x).stream()).toList());
//                    System.out.println(allMethods.size() + "Methods detected");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                logger.error(project+ " is not a directory");
            }
        }
        List<MethodDeclaration> sortedMethodDeclarations = allMethods.stream().sorted(Comparator.comparingInt(x -> x.getMethod().getLength())).toList();
        Integer fileName= 0;
        ListIterator<MethodDeclaration> iterator = sortedMethodDeclarations.listIterator(sortedMethodDeclarations.size());

        while (iterator.hasPrevious()) {
            MethodDeclaration method = iterator.previous();
            fileName++;
            JSONObject object = generateCodeImprovements(method, projectName);
            FileIO.writeJSONObjectToFile(object,Configurations.IMPROVEMENTS +"/"+projectName, String.valueOf(fileName));
            FileIO.readJSONObjectFromFile(Configurations.IMPROVEMENTS +"/"+projectName,String.valueOf(fileName));
            if (fileName==200){
                break;
            }
        }
    }

    /**
     * send request to GPT and return the response as a JSON object
     * @param decleration
     * @param projectName
     * @return
     */
    private JSONObject generateCodeImprovements(MethodDeclaration decleration, String projectName){
        JSONObject ob = new JSONObject();
        OpenAIRequestHandler requestHandler = new OpenAIRequestHandler();
        JSONArray response = requestHandler.getGPT4Response(Prompt.getPrompt(decleration.getMethod().toString()));
        ob.put("Method_Improvements",response);
        ob.put("Method_Name",decleration.getName());
        ob.put("File_Path",decleration.getUrl());
        ob.put("Old_Method",decleration.getMethod().toString());
        ob.put("Start",decleration.getStartCharacter());
        ob.put("Stop",decleration.getEndCharacter());
        ob.put("Project_Name",projectName);
        return ob;
    }
}
