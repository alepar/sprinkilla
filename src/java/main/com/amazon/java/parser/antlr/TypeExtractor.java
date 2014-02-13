package com.amazon.java.parser.antlr;

import com.amazon.java.TypeDefinition;
import com.amazon.java.TypeParameter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        final TypeParameter typeParameter = parent.getCurrentGenericContext().get(name);
        if (typeParameter != null) {
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
                fqcn, genericTypes, generic == null ? null : parent.getClassTypeParameterContext().get(generic)
        );
    }

}
