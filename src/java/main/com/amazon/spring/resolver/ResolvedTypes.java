package com.amazon.spring.resolver;

import com.amazon.spring.BeanDefinition;
import com.amazon.spring.ResolvedArguments;

public interface ResolvedTypes {
    ResolvedArguments getTypesFor(BeanDefinition bean);
}
