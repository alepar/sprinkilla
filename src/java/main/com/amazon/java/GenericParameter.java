package com.amazon.java;

public interface GenericParameter {
    String getName();

    BoundaryModifier getBoundaryModifier();

    TypeDefinition getBoundaryType();

    GenericContext getContext();

    public enum BoundaryModifier {
        SUPER, EXTENDS
    }
}
