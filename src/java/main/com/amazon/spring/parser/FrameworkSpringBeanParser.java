package com.amazon.spring.parser;

import com.amazon.spring.BeanDefinition;
import com.amazon.spring.ConstructorArgument;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;

import java.util.ArrayList;
import java.util.List;

public class FrameworkSpringBeanParser implements SpringBeanParser {

    @Override
    public BeanDefinition parse(String xml) {
        final SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
        reader.loadBeanDefinitions(new ByteArrayResource(xml.getBytes()));

        final String beanName = registry.getBeanDefinitionNames()[0];
        return convert(beanName, registry.getBeanDefinition(beanName));
    }

    private static BeanDefinition convert(String beanName, org.springframework.beans.factory.config.BeanDefinition beanDefinition) {
        return new SimpleBeanDefinition(beanName, beanDefinition.getBeanClassName(), convert(beanDefinition.getConstructorArgumentValues()));
    }

    private static List<ConstructorArgument> convert(ConstructorArgumentValues constructorArgumentValues) {
        final List<ConstructorArgument> args = new ArrayList<>();

        for (ConstructorArgumentValues.ValueHolder valueHolder : constructorArgumentValues.getGenericArgumentValues()) {
            args.add(convert(valueHolder));
        }

        return args;
    }

    private static ConstructorArgument convert(ConstructorArgumentValues.ValueHolder valueHolder) {
        return new SimpleConstructorArgument(valueHolder.getName(), convert(valueHolder.getName(), ((BeanDefinitionHolder) valueHolder.getValue()).getBeanDefinition()));
    }

    private static class SimpleBeanDefinition implements BeanDefinition {

        private final String name;
        private final String fqcn;
        private final List<ConstructorArgument> constructorArguments;

        private SimpleBeanDefinition(String name, String fqcn, List<ConstructorArgument> constructorArguments) {
            this.name = name;
            this.fqcn = fqcn;
            this.constructorArguments = constructorArguments;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getFqcn() {
            return fqcn;
        }

        @Override
        public List<ConstructorArgument> getConstructorArgs() {
            return constructorArguments;
        }
    }

    private static class SimpleConstructorArgument implements ConstructorArgument {

        private final String name;
        private final BeanDefinition beanDef;

        private SimpleConstructorArgument(String name, BeanDefinition beanDef) {
            this.name = name;
            this.beanDef = beanDef;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public BeanDefinition getBeanDefinition() {
            return beanDef;
        }
    }
}
