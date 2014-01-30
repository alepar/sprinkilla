package com.amazon.parser;

import java.io.Reader;

public interface JavaParser {

    ClassDefinition parse(Reader r);

}
