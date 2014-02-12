package com.amazon.java;

import java.util.List;

public interface ClassDefinition {
    TypeDefinition getType();

    List<MethodDefinition> getConstructors();

    List<TypeDefinition> getParentTypes();
}
