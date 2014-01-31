package com.amazon.parser.java;

public interface GenericArgument {
    String getName();

    BoundaryType getBoundaryType();

    String getBoundaryName();

    public enum BoundaryType {
        NONE, EXTENDS

    }
}
