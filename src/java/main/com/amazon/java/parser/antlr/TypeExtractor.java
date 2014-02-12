package com.amazon.java.parser.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;

import com.amazon.java.GenericParameter;
import com.amazon.java.TypeDefinition;

public class TypeExtractor extends StackTreeListener {

    private final SourceFileClassExtractor parent;

    private List<TypeDefinition> genericTypes = new ArrayList<>();
    private String fqcn;
    private String generic;

    private TypeExtractor child;

    public TypeExtractor(StackTreeWalker walker, SourceFileClassExtractor parent) {
        super(walker);
        this.parent = parent;
    }

    @Override
    public void enterClassOrInterfaceType(@NotNull JavaParser.ClassOrInterfaceTypeContext ctx) {
        final String name = ctx.getChild(0).getText();
        final GenericParameter genericParameter = parent.getCurrentGenericContext().get(name);
        if (genericParameter != null) {
            generic = name;
        } else {
            fqcn = parent.getImports().get(name) == null ? name : parent.getImports().get(name);
        }
    }

    @Override
    public void enterType(@NotNull JavaParser.TypeContext ctx) {
        child = new TypeExtractor(walker, parent);
        walker.push(child);
    }

    @Override
    public void exitType(@NotNull JavaParser.TypeContext ctx) {
        walker.pop();
    }

    @Override
    public void resume() {
        genericTypes.add(child.makeType());
    }

    public TypeDefinition makeType() {
        return new AntlrTypeDefinition(
                fqcn, genericTypes, generic == null ? null : parent.getClassGenericContext().get(generic)
        );
    }

}
