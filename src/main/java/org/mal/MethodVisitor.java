package org.mal;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;

public class MethodVisitor extends ASTVisitor {
    ArrayList<MethodDeclaration> methods = new ArrayList<>();
    @Override
    public boolean visit(MethodDeclaration node) {
        methods.add(node);
        return true;
    }
}
