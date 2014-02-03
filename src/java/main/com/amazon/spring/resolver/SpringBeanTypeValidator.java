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
        final ClassDefinition classDefinition = definitionProvider.getFor(bean.getClassname());
        final MethodDefinition method = matchSuitableConstructor(bean, classDefinition);

        final List<Boundary> boundaries = new ArrayList<>();
        for (int i=0; i<method.getArguments().size(); i++) {
            final TypeDefinition sourceType = method.getArguments().get(i).getType();
            final ClassDefinition sourceClass = definitionProvider.getFor(sourceType.getFqcn());
            final BeanDefinition constructorArgBean = bean.getConstructorArgs().get(i);
            final ClassDefinition constructorArgClass = definitionProvider.getFor(constructorArgBean.getClassname());
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
        throw new RuntimeException("parfenov, implement me!");
    }

    private static class Boundary {
    }
}
