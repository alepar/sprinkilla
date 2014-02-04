package com.amazon.spring;

import java.util.List;

public interface BeanDefinition {
    String getName();

    String getFqcn();

    List<ConstructorArgument> getConstructorArgs();

}
