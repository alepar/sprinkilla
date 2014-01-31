package com.amazon.java;

public class MapDefinitionProvider implements ClassDefinitionProvider {
    @Override
    public ClassDefinition getFor(String fqcn) {
        throw new RuntimeException("parfenov, implement me!");
    }

    public void addDefinition(ClassDefinition definition) {
        throw new RuntimeException("parfenov, implement me!");
    }
}
