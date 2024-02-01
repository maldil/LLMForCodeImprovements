package org.mal.apply;

public class Improvement {
    String improvedCode;
    String filePath;
    Integer start;
    Integer stop;

    public Improvement(String improvedCode, Integer start, Integer stop, String filePath) {
        this.improvedCode = improvedCode;
        this.start = start;
        this.stop = stop;
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getImprovedCode() {
        return improvedCode;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getStop() {
        return stop;
    }
}
