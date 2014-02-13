package com.amazon.java.parser.antlr;

import com.amazon.java.TypeDefinition;
import com.amazon.java.TypeParameter;
import com.amazon.java.TypeParameterContext;

class AntlrTypeParameter implements TypeParameter {

    private final String name;
    private final BoundaryModifier boundaryModifier;
    private final TypeDefinition boundaryType;
    private final TypeParameterContext context;

    AntlrTypeParameter(String name, BoundaryModifier boundaryModifier, TypeDefinition boundaryType, TypeParameterContext context) {
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
    public TypeParameterContext getContext() {
        return context;
    }
}
