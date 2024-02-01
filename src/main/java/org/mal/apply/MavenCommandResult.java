package org.mal.apply;

import java.util.List;

public class MavenCommandResult {
    private final int exitCode;
    private final List<String> output;

    public MavenCommandResult(int exitCode, List<String> output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public List<String> getOutput() {
        return output;
    }
}