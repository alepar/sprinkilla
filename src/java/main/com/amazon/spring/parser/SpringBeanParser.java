package com.amazon.spring.parser;

import com.amazon.spring.BeanDefinition;

public interface SpringBeanParser {

    BeanDefinition parse(String xml);

}
