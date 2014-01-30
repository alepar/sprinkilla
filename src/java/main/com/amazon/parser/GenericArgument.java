package com.amazon.parser;

public interface GenericArgument {
    String getName();

    BoundaryType getBoundaryType();

    String getBoundaryName();

    public enum BoundaryType {
        NONE, EXTENDS

    }
}
