package org.mal;

public class MethodDeclaration {
    private org.eclipse.jdt.core.dom.MethodDeclaration method;
    private String name;
    private Integer startCharacter;
    private Integer endCharacter;
    private String url;

    public MethodDeclaration(org.eclipse.jdt.core.dom.MethodDeclaration method, String name, Integer startCharacter, Integer endCharacter, String url) {
        this.method = method;
        this.name = name;
        this.startCharacter = startCharacter;
        this.endCharacter = endCharacter;
        this.url = url;

    }

    public org.eclipse.jdt.core.dom.MethodDeclaration getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public Integer getStartCharacter() {
        return startCharacter;
    }

    public Integer getEndCharacter() {
        return endCharacter;
    }

    public String getUrl() {
        return url;
    }
}
