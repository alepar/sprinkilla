package com.amazon.java;

public interface MutableClassDefinitionProvider extends ClassDefinitionProvider {
    void addDefinition(ClassDefinition definition);
}
