package com.amazon.java;

import java.util.List;

import com.amazon.java.parser.antlr.AntlrGenericContext;

public interface GenericContext {

    GenericParameter get(String name);
    void add(GenericParameter param);
    List<GenericParameter> getParams();

    GenericContext EMPTY = new AntlrGenericContext();

}
