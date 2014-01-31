package com.amazon.spring.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazon.java.ClassDefinition;
import com.amazon.java.ClassDefinitionProvider;
import com.amazon.java.MethodDefinition;
import com.amazon.spring.BeanDefinition;

public class GenericWildcardResolver {

    private final ClassDefinitionProvider definitionProvider;

    public GenericWildcardResolver(ClassDefinitionProvider definitionProvider) {
        this.definitionProvider = definitionProvider;
    }

    public ResolvedTypes resolve(BeanDefinition bean) {
        final ClassDefinition classDefinition = definitionProvider.getFor(bean.getClassname());
        final MethodDefinition method = matchSuitableConstructor(bean, classDefinition);

        final List<Boundary> boundaries = new ArrayList<>();
        for (int i=0; i<method.getArguments().size(); i++) {
            final ClassDefinition sourceType = method.getArguments().get(i).getType();
            final BeanDefinition constructorArg = bean.getConstructorArgs().get(i);
            final ClassDefinition constructorArgType = definitionProvider.getFor(constructorArg.getClassname());
            boundaries.addAll(inferBoundaries(sourceType, constructorArgType));
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
