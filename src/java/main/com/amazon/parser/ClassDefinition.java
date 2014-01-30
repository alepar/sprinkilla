package com.amazon.parser;

import java.util.List;

public interface ClassDefinition {
    String getName();

    List<GenericArgument> getGenericArguments();
}
