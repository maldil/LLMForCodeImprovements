package org.mal;
import org.eclipse.jgit.api.Git;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.log4j.Logger;
import io.vavr.control.Try;

public class MyGit {
    static Logger logger = Logger.getLogger(MyGit.class);

    /**
     * the method first check if the project exist in the path, otherwise it clone into the path
     * @param name
     * @param path
     * @return
     */
    public static String cloneIfNotExits(String name, String path) {
        try {
            if(!Files.exists(Path.of(path + "/" + name))|| FileIO.isDirEmpty(Path.of(path + "/" + name))){
                Try.of(() -> {
                    Git.cloneRepository().setURI("https://github.com/"+name+".git").setDirectory(new File(path + "/" + name)).call();
                    return null;
                }).onFailure(Throwable::printStackTrace);
                logger.warn("project "+name+ " cloned to "+path);
            }
            else {
                logger.warn("project "+name+ " exists in "+path);
            }
        } catch (IOException | NullPointerException e) {
            FileIO.deleteDFile(path+name);
            e.printStackTrace();
        }
        return path + "/" + name;
    }
}
