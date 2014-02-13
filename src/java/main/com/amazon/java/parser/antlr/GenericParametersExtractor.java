package com.amazon.java.parser.antlr;

import com.amazon.java.TypeDefinition;
import com.amazon.java.TypeParameter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenericParametersExtractor extends StackTreeListener {

    private final SourceFileClassExtractor parent;

    private final List<TypeParameter> typeParameters = new ArrayList<>();

    private String name;
    private TypeParameter.BoundaryModifier boundaryModifier;
    private TypeDefinition boundaryType;
    private TypeExtractor child;

    public GenericParametersExtractor(StackTreeWalker walker, SourceFileClassExtractor parent) {
        super(walker);
        this.parent = parent;
    }

    @Override
    public void enterTypeParameter(@NotNull JavaParser.TypeParameterContext ctx) {
        name = ctx.getChild(0).getText();
        if (ctx.getChildCount() == 1) {
            boundaryType = new AntlrTypeDefinition("java.lang.Object", Collections.<TypeDefinition>emptyList(), null);
            boundaryModifier = TypeParameter.BoundaryModifier.EXTENDS;
        } else {
            if ("super".equals(ctx.getChild(1).getText())) {
                boundaryModifier = TypeParameter.BoundaryModifier.SUPER;
            } else {
                boundaryModifier = TypeParameter.BoundaryModifier.EXTENDS;
            }
        }
    }

    @Override
    public void exitTypeParameter(@NotNull JavaParser.TypeParameterContext ctx) {
        typeParameters.add(new AntlrTypeParameter(
            name, boundaryModifier, boundaryType, parent.getCurrentGenericContext())
        );
    }

    @Override
    public void exitTypeParameters(@NotNull JavaParser.TypeParametersContext ctx) {
        walker.pop();

        for (TypeParameter parameter : typeParameters) {
            parent.getCurrentGenericContext().add(parameter);
        }
    }

    @Override
    public void enterType(@NotNull JavaParser.TypeContext ctx) {
        child = new TypeExtractor(walker, parent);
        walker.push(child);
    }

    @Override
    public void resume() {
        boundaryType = child.makeType();
    }

}
