package com.amazon.parser.java;

import java.util.List;

public interface ClassDefinition {
    String getName();

    List<GenericArgument> getGenericArguments();
}
