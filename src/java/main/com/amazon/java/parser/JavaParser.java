package com.amazon.java.parser;

import java.io.Reader;

import com.amazon.java.ClassDefinition;

public interface JavaParser {

    ClassDefinition parse(Reader r);

}
