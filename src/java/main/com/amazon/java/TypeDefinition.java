package com.amazon.java;

import java.util.List;

public interface TypeDefinition {

    String getFqcn();

    List<TypeDefinition> getGenericArguments();

    GenericParameter getGenericParam();
}
