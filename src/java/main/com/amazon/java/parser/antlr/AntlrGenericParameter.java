package com.amazon.java.parser.antlr;

import com.amazon.java.GenericContext;
import com.amazon.java.GenericParameter;
import com.amazon.java.TypeDefinition;

class AntlrGenericParameter implements GenericParameter {

    private final String name;
    private final BoundaryModifier boundaryModifier;
    private final TypeDefinition boundaryType;
    private final GenericContext context;

    AntlrGenericParameter(String name, BoundaryModifier boundaryModifier, TypeDefinition boundaryType, GenericContext context) {
        this.name = name;
        this.boundaryModifier = boundaryModifier;
        this.boundaryType = boundaryType;
        this.context = context;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BoundaryModifier getBoundaryModifier() {
        return boundaryModifier;
    }

    @Override
    public TypeDefinition getBoundaryType() {
        return boundaryType;
    }

    @Override
    public GenericContext getContext() {
        return context;
    }
}
