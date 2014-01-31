package com.amazon.java;

public interface ClassDefinitionProvider {
    ClassDefinition getFor(String fqcn);
}
