package com.amazon.java;

public interface GenericArgument {
    String getName();

    BoundaryType getBoundaryType();

    String getBoundaryName();

    public enum BoundaryType {
        NO_WILDCARD, EXTENDS
    }
}
