package com.amazon.java;

import java.io.StringReader;

import com.amazon.java.parser.JavaSourceParser;

public class JavaSourceRepository implements TypeHierarchy, ClassDefinitionProvider {

    private final IndexedTypeHierarchy typeHierarchy;
    private final MutableClassDefinitionProvider definitionProvider;
    private final JavaSourceParser javaSourceParser;

    public JavaSourceRepository(MutableClassDefinitionProvider definitionProvider, JavaSourceParser javaSourceParser) {
        this.javaSourceParser = javaSourceParser;
        this.definitionProvider = definitionProvider;
        this.typeHierarchy = new IndexedTypeHierarchy(definitionProvider);
    }

    public void addSource(String source) {
        final ClassDefinition classDefinition = javaSourceParser.parse(new StringReader(source));
        definitionProvider.addDefinition(classDefinition);
    }

    @Override
    public ClassDefinition getFor(String fqcn) {
        return definitionProvider.getFor(fqcn);
    }

    @Override
    public GuessedTypeParameters isAssignable(TypeDefinition src, TypeDefinition dest) {
        return typeHierarchy.isAssignable(src, dest);
    }

}
