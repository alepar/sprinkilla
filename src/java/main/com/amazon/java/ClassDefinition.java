package com.amazon.java;

import java.util.List;

public interface ClassDefinition {
    String getFqcn();

    List<GenericArgument> getGenericArguments();
}
