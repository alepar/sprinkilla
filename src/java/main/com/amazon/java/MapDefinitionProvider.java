package com.amazon.java;

import java.util.HashMap;
import java.util.Map;

public class MapDefinitionProvider implements ClassDefinitionProvider {

    private final Map<String, ClassDefinition> definitions = new HashMap<>();

    @Override
    public ClassDefinition getFor(String fqcn) {
        final ClassDefinition definition = definitions.get(fqcn);
        if (definition == null) {
            throw new RuntimeException("could not find definition for " + fqcn);
        }
        return definition;
    }

    public void addDefinition(ClassDefinition definition) {
        definitions.put(definition.getType().getFqcn(), definition);
    }
}
