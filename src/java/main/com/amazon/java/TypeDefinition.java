package com.amazon.java;

import com.amazon.java.parser.antlr.AntlrTypeDefinition;

import java.util.Collections;
import java.util.List;

public interface TypeDefinition {

    AntlrTypeDefinition JAVA_LANG_OBJECT = new AntlrTypeDefinition("java.lang.Object", Collections.<TypeDefinition>emptyList(), null);

    String getFqcn();

    List<TypeDefinition> getGenericTypeParameters();

    TypeParameter getTypeParameter();
}
