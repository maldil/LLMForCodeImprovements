package org.mal.stats;

import org.mal.FileIO;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupImprovements {
    /**
     * This main function analyzes the improvements in the resources/improvements.txt and tries to group them based on regex matching.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        List<String> improvements = getImprovements().stream().map(s -> s.replaceAll("^\\d+\\. ", "")).toList();
        System.out.println("Total Improvements "+improvements.size());
        Map<String, Integer> improvementsCounts = getImprovementsCounts();
        HashMap<Pattern, List<String>> patternAndImprvements = new HashMap<>();
        HashMap<Pattern, Integer> patternToInstances = new HashMap<>();
        System.out.println("Total instances ::: "+improvementsCounts.values().stream().mapToInt(Integer::intValue).sum());
        for (Pattern s : getStringPatterns()) {
            patternAndImprvements.put(s,improvements.stream().filter(x -> s.matcher(x).find()).toList());
            patternToInstances.put(s,improvements.stream().filter(x -> s.matcher(x).find()).map(improvementsCounts::get).reduce(Integer::sum).get());
            improvements = improvements.stream().filter(x -> !s.matcher(x).find()).toList();
        }
        patternAndImprvements.entrySet().forEach(x-> System.out.println(x.getKey().pattern()+" ::: "+x.getValue().size()+" ::: "+x.getValue().toString()));

        List<String> nonMatched = improvements.stream().filter(x -> getStringPatterns().stream().noneMatch(y -> y.matcher(x).find())).toList();
        System.out.println("Size of non-matched improvements "+nonMatched.size());
        nonMatched.forEach(System.out::println);
        patternToInstances.entrySet().forEach(x -> System.out.println(x.getKey()+" ::: "+ x.getValue()));
    }


    /**
     * get impvements and their frequency
     * @return
     * @throws IOException
     */
    private static Map<String, Integer> getImprovementsCounts() throws IOException {
        Map<String, Integer> collect = FileIO.readFileLineByLineNIO(Path.of(getFileInResources("improvements.txt").getPath()))
                .map(q -> q.replaceAll("^\\d+\\. ", ""))
                .map(line -> line.split(","))
                .filter(z -> z.length == 2)
                .filter(parts -> parts[0] != null && parts[1] != null)
                .filter(parts -> !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty())
                .collect(Collectors.toMap(x -> x[0],
                        y -> Integer.parseInt(y[1].trim()), (existing, replacement) -> existing));
        return collect;
    }



    /**
     * get a list of improvements in the file "improvements.txt"
     * @return
     * @throws IOException
     */
    private static List<String> getImprovements() throws IOException {
        return FileIO.readFileLineByLineNIO(Path.of(getFileInResources("improvements.txt").getPath())).map(x -> x.split(",")[0]).toList();
    }


    /**
     * Users can define list of regex patterns inside the method, it converts string regex into a pattern
     * @return
     */
    private static List<Pattern> getStringPatterns(){
        List<String> list = List.of("Use enhanced for[- ]loop.*",
                "Use foreach loop instead of.*",
                "(Use|Improve|Update|Refactor).* variable names?|Rename .*?",
                "Extract .*?",
                "Use (a )?StringBuilder(\\.append\\(\\))? (for|instead of|to)? .*",
                "Use lambda expression.*",
                "Use ([\\w\\.]+\\(.*?\\)) instead of (!?[\\w\\.]+\\(.*?\\))( .*?)?",
                "Use (\\w+\\.)?(\\w+\\(\\)) instead of null check.*",
                "Use (\\w+\\.)?(\\w+\\(\\)) method instead of.*",
                "Use .*? method[.!?]?$",
                "Consistent indentation|Improve code formatting|Structure code .*?",
                "Use try-with-resources( for .*?)?",
                "Use switch statement for *.?",
                "Add comments *.?",
                "Use diamond operator .*?");
        return list.stream().map(Pattern::compile).toList();
    }

    /**
     *
     * @param fileName file name in resources folder that need path
     * @return URL for the path
     */
    private static URL getFileInResources(String fileName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResource(fileName);
    }

}
