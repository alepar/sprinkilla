package com.amazon.java.parser.antlr;

import java.util.List;

import com.amazon.java.GenericParameter;
import com.amazon.java.TypeDefinition;

class AntlrTypeDefinition implements TypeDefinition {

    private final String fqcn;
    private final List<TypeDefinition> genericTypes;
    private final GenericParameter genericParam;

    public AntlrTypeDefinition(String fqcn, List<TypeDefinition> genericTypes, GenericParameter genericParam) {
        this.fqcn = fqcn;
        this.genericTypes = genericTypes;
        this.genericParam = genericParam;
    }

    @Override
    public String getFqcn() {
        return fqcn;
    }

    @Override
    public List<TypeDefinition> getGenericArguments() {
        return genericTypes;
    }

    @Override
    public GenericParameter getGenericParam() {
        return genericParam;
    }
}
