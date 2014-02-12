package com.amazon.java.parser.antlr;

import com.amazon.java.TypeDefinition;
import com.amazon.java.Variable;

class AntlrVariable implements Variable {
    private final String name;
    private final TypeDefinition type;

    AntlrVariable(String name, TypeDefinition type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public TypeDefinition getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }
}
