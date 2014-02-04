package com.amazon.spring.resolver;

import com.amazon.java.ClassDefinition;
import com.amazon.java.ClassDefinitionProvider;
import com.amazon.java.MethodDefinition;
import com.amazon.java.TypeDefinition;
import com.amazon.spring.BeanDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpringBeanTypeValidator {

    private final ClassDefinitionProvider definitionProvider;

    public SpringBeanTypeValidator(ClassDefinitionProvider definitionProvider) {
        this.definitionProvider = definitionProvider;
    }

    public ResolvedTypes validateAndResolve(BeanDefinition bean) {
        final ClassDefinition classDefinition = definitionProvider.getFor(bean.getFqcn());
        final MethodDefinition method = matchSuitableConstructor(bean, classDefinition);

        final List<Boundary> boundaries = new ArrayList<>();
        for (int i=0; i<method.getArguments().size(); i++) {
            final TypeDefinition sourceType = method.getArguments().get(i).getType();
            final ClassDefinition sourceClass = definitionProvider.getFor(sourceType.getFqcn());
            final BeanDefinition constructorArgBean = bean.getConstructorArgs().get(i).getBeanDefinition();
            final ClassDefinition constructorArgClass = definitionProvider.getFor(constructorArgBean.getFqcn());
            boundaries.addAll(inferBoundaries(sourceClass, constructorArgClass));
        }

        return resolveBoundaries(boundaries);
    }

    private ResolvedTypes resolveBoundaries(List<Boundary> boundaries) {
        throw new RuntimeException("parfenov, implement me!");
    }

    private Collection<Boundary> inferBoundaries(ClassDefinition sourceType, ClassDefinition constructorArgType) {
        throw new RuntimeException("parfenov, implement me!");
    }

    private MethodDefinition matchSuitableConstructor(BeanDefinition bean, ClassDefinition classDefinition) {
        final int argCount = bean.getConstructorArgs().size();
        for (MethodDefinition constructor : classDefinition.getConstructors()) {
            if (constructor.getArguments().size() == argCount) {
                return constructor;
            }
        }

        throw new RuntimeException("could not find matching constructor for bean named " + bean.getName());
    }

    private static class Boundary {
    }
}
