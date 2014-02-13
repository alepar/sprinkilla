package com.amazon.java;

public interface TypeParameter {
    String getName();

    BoundaryModifier getBoundaryModifier();

    TypeDefinition getBoundaryType();

    TypeParameterContext getContext();

    public enum BoundaryModifier {
        SUPER, EXTENDS
    }
}
