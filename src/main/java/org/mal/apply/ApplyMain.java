package org.mal.apply;

import org.mal.ProcessAST;

public class ApplyMain {
    public static void main(String[] args) throws Exception {
        // Take the improved methods in the improvements folder, apply them to the projects, and then compile it.
        ApplyChanges p = new ApplyChanges();
        p.processImprovements();
    }
}
