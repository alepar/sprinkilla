package com.amazon.java;

import java.util.List;

public interface TypeParameterContext {

    TypeParameter get(String name);
    void add(TypeParameter param);
    List<TypeParameter> getParams();

}
