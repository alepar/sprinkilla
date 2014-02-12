package com.amazon.java;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IndexedTypeHierarchy implements MutableTypeHierarchy {

    private final Map<String, Set<ClassDefinition>> parents = new HashMap<>();

    @Override
    public void addDefinition(ClassDefinition definition) {
        throw new RuntimeException("parfenov, implement me!");
    }

    @Override
    public boolean isAssignable(TypeDefinition src, TypeDefinition dest) {
        throw new RuntimeException("parfenov, implement me!");
    }
}
