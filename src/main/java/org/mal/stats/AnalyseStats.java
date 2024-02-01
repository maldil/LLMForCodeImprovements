package org.mal.stats;

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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AnalyseStats {
    /**
     * This main function computes the statistics of improvements and compile errors in the folders IMPROVEMENTS
     * and COMPILE_ERRORS, respectively.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        analyseImprvements();
        analyseCompileErrors();
    }

    private static void analyseCompileErrors() throws FileNotFoundException {
        ArrayList<Path> allFiles = new ArrayList<>();
        ArrayList<String> allErrors = new ArrayList<>();
        for (String project : getProjectList()) {
            Path projectPath = Paths.get(Configurations.COMPILE_ERRORS, project);
            if (!Files.exists(projectPath) || !Files.isDirectory(projectPath)) {
                throw new FileNotFoundException("Improvements cannot be found for " + project);
            }
            allFiles.addAll(FileIO.getAllFilesInDirectory(projectPath, ".txt")) ;
        }
        Pattern pattern = Pattern.compile(".*?(/[^ ]+?\\.java).*");
        for (Path file : allFiles) {
            List<String> listOfErrors = Arrays.stream(Objects.requireNonNull(FileIO.readStringFromFile(file))
                    .split("\n")).filter(x -> x.startsWith("[ERROR]")).toList();
            List<String> pathContainingErrors = listOfErrors.stream()
                    .filter(error -> pattern.matcher(error).find())
                    .toList();
            Pattern pattern2 = Pattern.compile("/[^ ]+?\\.java:\\[\\d+,\\d+\\](.*)");

            List<String> modifiedErrors = pathContainingErrors.stream()
                    .map(error -> {
                        Matcher matcher = pattern2.matcher(error);
                        if (matcher.find()) {
                            return matcher.group(1).trim(); // Capture the message after the file path and line number details
                        }
                        return ""; // Return an empty string or handle as needed if the pattern is not found
                    })
                    .toList();
            allErrors.addAll(modifiedErrors);
//
        }

        allErrors.stream().collect(Collectors.groupingBy(s->s,Collectors.counting())).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).forEach(entry ->
                    System.out.println(entry.getKey() + "::: " + entry.getValue()));

    }

    private static void analyseImprvements() throws FileNotFoundException {
        for (String project : getProjectList()) {
            Path projectPath = Paths.get(Configurations.IMPROVEMENTS, project);
            if (!Files.exists(projectPath) || !Files.isDirectory(projectPath)) {
                throw new FileNotFoundException("Improvements cannot be found for " + project);
            }
            List<Path> jsons = FileIO.getAllFilesInDirectory(Path.of(Configurations.IMPROVEMENTS +"/"+project+"/"), "").stream().filter(x-> {
                try {
                    return x.getFileName().toString().matches("\\d+") && validJSON(x);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
            List<JSONObject> listOfImpsInJson = jsons.stream().map(file -> {
                try {
                    return FileIO.readJSONObjectFromFile(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
            List<JSONObject> allImprovements = listOfImpsInJson.stream()
                    // Extract JSONArray from each JSONObject
                    .map(x -> x.getJSONArray("Method_Improvements"))
                    // Convert JSONArray to Stream<JSONObject>
                    .flatMap(jsonArray -> StreamSupport.stream(jsonArray.spliterator(), false))
                    // Cast each element of the stream to JSONObject
                    .map(obj -> (JSONObject) obj)
                    // Collect all JSONObject into a List
                    .toList();
            List<String> topics = allImprovements.stream().map(x -> x.getJSONArray("Improvements"))
                    .flatMap(jsonArray -> StreamSupport.stream(jsonArray.spliterator(), false))
                    .map(obj -> (JSONObject) obj).map(x -> x.getString("Improvement")).toList();
            Map<String, List<String>> improvementsAndDescriptions = allImprovements.stream()
                    .map(x -> x.getJSONArray("Improvements"))
                    .flatMap(jsonArray -> StreamSupport.stream(jsonArray.spliterator(), false))
                    .map(obj -> (JSONObject) obj)
                    .collect(Collectors.groupingBy(
                            x -> x.getString("Improvement"),
                            Collectors.mapping(
                                    x -> x.getString("Description"),
                                    Collectors.toList()
                            )
                    ));
            Map<String, Long> distributionOfTitles = topics.stream()
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
            distributionOfTitles.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(entry ->
                            System.out.println(entry.getKey() + ": " + entry.getValue()));


            System.out.println(distributionOfTitles);
            System.out.println(improvementsAndDescriptions);

        }



    }

    /**
     * Reads a list of projects from a file.
     *
     * @return A List of project names, or an empty list if an error occurs.
     */
    private static List<String> getProjectList(){
        File file = new File("selected_repos_updated.txt");
        try {
            return FileUtils.readLines(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static boolean validJSON(Path json) throws IOException {
        JSONObject object = FileIO.readJSONObjectFromFile(json);
        JSONArray ims = object.getJSONArray("Method_Improvements");
        for (Object o : ims) {
            JSONObject im = (JSONObject)o;
            if (im.keySet().contains("error")){
                if(im.getString("error").equals("An unexpected error occurred")){
                    return false;
                }
            }
        }
        return true;
    }
}
