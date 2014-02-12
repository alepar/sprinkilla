package com.amazon.java;

public class IndexedTypeHierarchy implements MutableTypeHierarchy {
    @Override
    public void addDefinition(ClassDefinition definition) {
        throw new RuntimeException("parfenov, implement me!");
    }

    @Override
    public boolean isAssignable(TypeDefinition src, TypeDefinition dest) {
        throw new RuntimeException("parfenov, implement me!");
    }
}
