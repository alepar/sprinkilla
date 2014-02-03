package com.amazon.java;

public interface GenericParameter {
    String getName();

    BoundaryType getBoundaryType();

    String getBoundaryFqcn();

    public enum BoundaryType {
        NO_WILDCARD, EXTENDS
    }
}
