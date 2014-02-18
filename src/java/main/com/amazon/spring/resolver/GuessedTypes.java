package com.amazon.spring.resolver;

import com.amazon.spring.BeanDefinition;
import com.amazon.spring.GuessedTypeParameters;

public interface GuessedTypes {
    GuessedTypeParameters getTypesFor(BeanDefinition bean);

    boolean isValid();
}
