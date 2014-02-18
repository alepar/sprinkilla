package com.amazon.java;

public interface TypeHierarchy {
    GuessedTypeParameters isAssignable(TypeDefinition src, TypeDefinition dest);

    interface GuessedTypeParameters {
        TypeDefinition typeFor(TypeParameter param);
    }
}
