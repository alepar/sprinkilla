package com.amazon.spring.resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazon.java.ClassDefinition;
import com.amazon.java.ClassDefinitionProvider;
import com.amazon.java.MethodDefinition;
import com.amazon.java.TypeDefinition;
import com.amazon.java.TypeHierarchy;
import com.amazon.java.TypeParameter;
import com.amazon.spring.BeanDefinition;

public class SpringBeanTypeValidator {

    public static final InvalidConfiguration INVALID = new InvalidConfiguration();

    private final ClassDefinitionProvider definitionProvider;
    private final TypeHierarchy typeHierarchy;

    public SpringBeanTypeValidator(ClassDefinitionProvider definitionProvider, TypeHierarchy typeHierarchy) {
        this.definitionProvider = definitionProvider;
        this.typeHierarchy = typeHierarchy;
    }

    public com.amazon.spring.resolver.GuessedTypes validateAndResolve(BeanDefinition bean) {
        final ClassDefinition classDefinition = definitionProvider.getFor(bean.getFqcn());
        final MethodDefinition method = matchSuitableConstructor(bean, classDefinition);

        final List<TypeHierarchy.GuessedTypeParameters> allResolvedTypes = new ArrayList<>(method.getArguments().size());
        for (int i=0; i<method.getArguments().size(); i++) {
            final TypeDefinition sourceType = method.getArguments().get(i).getType();
            final BeanDefinition constructorArgBean = bean.getConstructorArgs().get(i).getBeanDefinition();
            final ClassDefinition constructorArgClass = definitionProvider.getFor(constructorArgBean.getFqcn());
            final TypeDefinition constructorArgType = constructorArgClass.getType();

            final TypeHierarchy.GuessedTypeParameters resolvedTypes = typeHierarchy.isAssignable(constructorArgType, sourceType);
            if (resolvedTypes == null) {
                return INVALID;
            } else {
                allResolvedTypes.add(resolvedTypes);
            }
        }

        return new GuessedTypes(definitionProvider, allResolvedTypes);
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

    private static class InvalidConfiguration implements com.amazon.spring.resolver.GuessedTypes {
        @Override
        public com.amazon.spring.GuessedTypeParameters getTypesFor(BeanDefinition bean) {
            return null;
        }

        @Override
        public boolean isValid() {
            return false;
        }
    }

    private static class GuessedTypes implements com.amazon.spring.resolver.GuessedTypes {

        private final ClassDefinitionProvider definitionProvider;
        private final List<TypeHierarchy.GuessedTypeParameters> allResolvedTypes;

        private GuessedTypes(ClassDefinitionProvider definitionProvider, List<TypeHierarchy.GuessedTypeParameters> allResolvedTypes) {
            this.definitionProvider = definitionProvider;
            this.allResolvedTypes = allResolvedTypes;
        }

        @Override
        public com.amazon.spring.GuessedTypeParameters getTypesFor(BeanDefinition bean) {
            final ClassDefinition classDefinition = definitionProvider.getFor(bean.getFqcn());
            final List<TypeDefinition> typeParameters = classDefinition.getType().getGenericTypeParameters();

            final Map<String, String> resolved = new HashMap<>();
            for (TypeDefinition typeParameter : typeParameters) {
                resolved.put(typeParameter.getTypeParameter().getName(), findTypeFor(typeParameter.getTypeParameter()));
            }

            return new GuessedTypeParameters(resolved);
        }

        private String findTypeFor(TypeParameter typeParameter) {
            for (TypeHierarchy.GuessedTypeParameters resolvedTypes : allResolvedTypes) {
                final TypeDefinition typeDefinition = resolvedTypes.typeFor(typeParameter);
                if (typeDefinition != null) {
                    return typeDefinition.getFqcn();
                }
            }
            return null;
        }

        @Override
        public boolean isValid() {
            return true;
        }

    }

    private static class GuessedTypeParameters implements com.amazon.spring.GuessedTypeParameters {
        private final Map<String, String> resolved;

        public GuessedTypeParameters(Map<String, String> resolved) {
            this.resolved = resolved;
        }

        @Override
        public String getFqcnFor(String paramName) {
            return resolved.get(paramName);
        }
    }
}
