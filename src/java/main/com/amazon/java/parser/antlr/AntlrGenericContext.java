package com.amazon.java.parser.antlr;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.amazon.java.GenericContext;
import com.amazon.java.GenericParameter;

public class AntlrGenericContext implements GenericContext {

    private final Map<String, GenericParameter> params = new LinkedHashMap<>();

    @Override
    public GenericParameter get(String name) {
        return params.get(name);
    }

    @Override
    public void add(GenericParameter param) {
        params.put(param.getName(), param);
    }

    @Override
    public List<GenericParameter> getParams() {
        return new ArrayList<>(params.values());
    }
}
