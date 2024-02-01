package org.mal;

import org.apache.log4j.Logger;

import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileIO {
    static Logger logger = Logger.getLogger(FileIO.class);

    public static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    /**
     * delete a directory
     * @param directoryPath
     */
    public static void deleteDFile(String directoryPath){
        Path directory = Paths.get(directoryPath);
        if (Files.exists(directory)) {
            try {
                Files.walk(directory)
                        .sorted((path1, path2) -> -path1.compareTo(path2))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                logger.error("Unable to delete the file: " + path);
                            }
                        });
                logger.info("Directory deleted successfully.");
            } catch (IOException e) {
                logger.error("Unable to delete the directory: " + directory);
            }
        } else {
            logger.error("Directory does not exist.");
        }
    }

    /**
     * Reads a file line by line and prints each line to the console using java.nio.file.
     *
     * @param filePath The path of the file to be read.
     * @return
     * @throws IOException If an I/O error occurs.
     */
    public static Stream<String> readFileLineByLineNIO(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        try (Stream<String> lines = Files.lines(path)) {
            return lines;
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            // Re-throw the exception to allow the caller to handle it
            throw e;
        }
    }

    /**
     *
     * @param filePath Path to the file
     * @return  the content of the file as Stream<String>
     * @throws IOException If an I/O eror occurs
     */
    public static Stream<String> readFileLineByLineNIO(Path filePath) throws IOException {
        return Files.lines(filePath);
    }

    /**
     *
     * @param inputFile
     * @return
     */
    public static String readStringFromFile(String inputFile) {
        try {
            return new String(Files.readAllBytes(Paths.get(inputFile)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param inputFile
     * @return
     */
    public static String readStringFromFile(Path inputFile) {
        try {
            return new String(Files.readAllBytes(inputFile));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param jsonObject
     * @param fileDirectory
     * @param fileName
     * @throws IOException
     */

    public static void writeJSONObjectToFile(JSONObject jsonObject, String fileDirectory, String fileName) throws IOException {
        Path directoryPath = Paths.get(fileDirectory);
        Path filePath = directoryPath.resolve(fileName);

        // Create the directory hierarchy if it does not exist
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Check if the file already exists
        if (Files.exists(filePath)) {
            throw new IOException("File already exists: " + filePath);
        }

        try (FileWriter file = new FileWriter(filePath.toString())) {
            file.write(jsonObject.toString(4)); // Write the JSON object to file with indentation
            file.flush();
        }
    }

    /**
     *
     * @param fileDirectory
     * @param fileName
     * @return
     * @throws IOException
     */
    public static JSONObject readJSONObjectFromFile(String fileDirectory, String fileName) throws IOException {
        Path filePath = Paths.get(fileDirectory, fileName);
        // Check if the file exists
        if (!Files.exists(filePath)) {
            throw new IOException("File does not exist: " + filePath);
        }

        String content = new String(Files.readAllBytes(filePath));
        return new JSONObject(content);
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static JSONObject readJSONObjectFromFile(Path file) throws IOException {
        // Check if the file exists
        if (!Files.exists(file)) {
            throw new IOException("File does not exist: " + file);
        }
        String content = new String(Files.readAllBytes(file));
        return new JSONObject(content);
    }

    /**
     *
     * @param folder
     * @param extension
     * @return
     */
    public static List<Path> getAllFilesInDirectory(Path folder, String extension){
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(extension)).toList();
//                    System.out.println(allMethods.size() + "Methods detected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    /**
     * @param source
     * @param backup
     * @throws IOException
     */
    public static void backupFile(Path source, Path backup) throws IOException {
        Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
    }


    /**
     *
     * @param file
     * @param newContent
     * @throws IOException
     */
    public static void modifyFileContent(Path file, String newContent) throws IOException {
        Files.writeString(file, newContent, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     *
     * @param backup
     * @param original
     * @throws IOException
     */
    public static void restoreFile(Path backup, Path original) throws IOException {
        Files.move(backup, original, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Writes an ArrayList of strings to a file, each on a new line.
     *
     * @param filePath The path of the file to write to.
     * @param lines The ArrayList of strings to write.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeLinesToFile(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent()); // Create directory structure if it doesn't exist
        Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}
