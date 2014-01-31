package com.amazon.parser.java;

import java.io.Reader;

public interface JavaParser {

    ClassDefinition parse(Reader r);

}
