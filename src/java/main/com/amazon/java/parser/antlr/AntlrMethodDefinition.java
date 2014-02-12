package com.amazon.java.parser.antlr;

import java.util.List;

import com.amazon.java.MethodDefinition;
import com.amazon.java.Variable;

class AntlrMethodDefinition implements MethodDefinition {
    private final List<Variable> vars;
    private final String name;

    AntlrMethodDefinition(List<Variable> vars, String name) {
        this.vars = vars;
        this.name = name;
    }

    @Override
    public List<Variable> getArguments() {
        return vars;
    }

    @Override
    public String getName() {
        return name;
    }
}
