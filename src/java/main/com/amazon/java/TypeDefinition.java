package com.amazon.java;

import java.util.Collections;
import java.util.List;

import com.amazon.java.parser.antlr.AntlrTypeDefinition;

public interface TypeDefinition {

    AntlrTypeDefinition JAVA_LANG_OBJECT = new AntlrTypeDefinition("java.lang.Object", Collections.<TypeDefinition>emptyList(), null);

    String getFqcn();

    List<TypeDefinition> getGenericArguments();

    GenericParameter getGenericParam();
}
