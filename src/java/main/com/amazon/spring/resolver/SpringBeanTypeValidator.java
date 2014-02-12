package com.amazon.spring.resolver;

import com.amazon.java.ClassDefinition;
import com.amazon.java.ClassDefinitionProvider;
import com.amazon.java.MethodDefinition;
import com.amazon.java.TypeDefinition;
import com.amazon.java.TypeHierarchy;
import com.amazon.spring.BeanDefinition;
import com.amazon.spring.ResolvedArguments;

public class SpringBeanTypeValidator {

    private final ClassDefinitionProvider definitionProvider;
    private final TypeHierarchy typeHierarchy;

    public SpringBeanTypeValidator(ClassDefinitionProvider definitionProvider, TypeHierarchy typeHierarchy) {
        this.definitionProvider = definitionProvider;
        this.typeHierarchy = typeHierarchy;
    }

    public ResolvedTypes validateAndResolve(BeanDefinition bean) {
        final ClassDefinition classDefinition = definitionProvider.getFor(bean.getFqcn());
        final MethodDefinition method = matchSuitableConstructor(bean, classDefinition);

        for (int i=0; i<method.getArguments().size(); i++) {
            final TypeDefinition sourceType = method.getArguments().get(i).getType();
            final BeanDefinition constructorArgBean = bean.getConstructorArgs().get(i).getBeanDefinition();
            final ClassDefinition constructorArgClass = definitionProvider.getFor(constructorArgBean.getFqcn());
            final TypeDefinition constructorArgType = constructorArgClass.getType();

            if (!typeHierarchy.isAssignable(constructorArgType, sourceType)) {
                return new InvalidResolvedTypes();
            }
        }

        return new ValidResolvedTypes();
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

    private static class InvalidResolvedTypes implements ResolvedTypes {
        @Override
        public ResolvedArguments getTypesFor(BeanDefinition bean) {
            return null;
        }

        @Override
        public boolean isValid() {
            return false;
        }
    }

    private static class ValidResolvedTypes implements ResolvedTypes {
        @Override
        public ResolvedArguments getTypesFor(BeanDefinition bean) {
            return null;
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }
}
