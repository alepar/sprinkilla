package com.amazon.java;

import java.io.StringReader;

import com.amazon.java.parser.JavaSourceParser;

public class JavaSourceRepository implements TypeHierarchy, ClassDefinitionProvider {

    private final MutableTypeHierarchy typeHierarchy;
    private final MutableClassDefinitionProvider definitionProvider;
    private final JavaSourceParser javaSourceParser;

    public JavaSourceRepository(MutableTypeHierarchy typeHierarchy, MutableClassDefinitionProvider definitionProvider, JavaSourceParser javaSourceParser) {
        this.typeHierarchy = typeHierarchy;
        this.definitionProvider = definitionProvider;
        this.javaSourceParser = javaSourceParser;
    }

    public void addSource(String source) {
        final ClassDefinition classDefinition = javaSourceParser.parse(new StringReader(source));
        definitionProvider.addDefinition(classDefinition);
        typeHierarchy.addDefinition(classDefinition);
    }

    @Override
    public ClassDefinition getFor(String fqcn) {
        return definitionProvider.getFor(fqcn);
    }

    @Override
    public boolean isAssignable(TypeDefinition src, TypeDefinition dest) {
        return typeHierarchy.isAssignable(src, dest);
    }

}
