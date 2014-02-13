package com.amazon.java.parser.antlr;

import com.amazon.java.TypeParameter;
import com.amazon.java.TypeParameterContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AntlrTypeParameterContext implements TypeParameterContext {

    private final Map<String, TypeParameter> params = new LinkedHashMap<>();

    @Override
    public TypeParameter get(String name) {
        return params.get(name);
    }

    @Override
    public void add(TypeParameter param) {
        params.put(param.getName(), param);
    }

    @Override
    public List<TypeParameter> getParams() {
        return new ArrayList<>(params.values());
    }
}
