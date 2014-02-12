package com.amazon.java.parser.antlr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;

import com.amazon.java.GenericParameter;
import com.amazon.java.TypeDefinition;

public class GenericParametersExtractor extends StackTreeListener {

    private final SourceFileClassExtractor parent;

    private final List<GenericParameter> genericParameters = new ArrayList<>();

    private String name;
    private GenericParameter.BoundaryModifier boundaryModifier;
    private TypeDefinition boundaryType;
    private TypeExtractor child;

    public GenericParametersExtractor(StackTreeWalker walker, SourceFileClassExtractor parent) {
        super(walker);
        this.parent = parent;
    }

    @Override
    public void enterTypeParameter(@NotNull JavaParser.TypeParameterContext ctx) {
        name = ctx.getChild(0).getText();
        boundaryModifier = GenericParameter.BoundaryModifier.EXTENDS;
        if (ctx.getChildCount() == 1) {
            boundaryType = new AntlrTypeDefinition("java.lang.Object", Collections.<TypeDefinition>emptyList(), null);
        }
    }

    @Override
    public void exitTypeParameter(@NotNull JavaParser.TypeParameterContext ctx) {
        genericParameters.add(new AntlrGenericParameter(
            name, boundaryModifier, boundaryType, parent.getCurrentGenericContext())
        );
    }

    @Override
    public void exitTypeParameters(@NotNull JavaParser.TypeParametersContext ctx) {
        walker.pop();

        for (GenericParameter parameter : genericParameters) {
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
