package com.amazon.spring;

import com.amazon.java.JavaSourceRepository;
import com.amazon.java.MapDefinitionProvider;
import com.amazon.java.parser.antlr.AntlrJavaSourceParser;
import com.amazon.spring.parser.FrameworkSpringBeanParser;
import com.amazon.spring.parser.SpringBeanParser;
import com.amazon.spring.resolver.GuessedTypes;
import com.amazon.spring.resolver.SpringBeanTypeValidator;
import org.junit.Test;

import static com.amazon.spring.parser.FrameworkSpringBeanParserTest.wrapBeanDef;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SpringBeanTypeValidatorTest {

    private final JavaSourceRepository repository = new JavaSourceRepository(new MapDefinitionProvider(), new AntlrJavaSourceParser());

    private final SpringBeanParser springBeanParser = new FrameworkSpringBeanParser();

    @Test
    public void passesValidationWhenExpectedClassAndPassedInAreTheSame() throws Exception {
        final String xml = wrapBeanDef(
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean id=\"oneArg\" class=\"com.amazon.SomeType\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n");
        final String sourceForNumberProcessor =
                "package com.amazon;\n" +
                "\n" +
                "import com.amazon.SomeType;\n" +
                "\n" +
                "public class NumberProcessor {\n" +
                "    public NumberProcessor(SomeType sometype) {}\n" +
                "}";
        final String sourceForListOfDoubles =
                "package com.amazon;\n" +
                "\n" +
                "public class SomeType { }";
        repository.addSource(sourceForNumberProcessor);
        repository.addSource(sourceForListOfDoubles);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);
        final GuessedTypes types = resolver.validateAndResolve(bean);

        assertThat(types.isValid(), equalTo(true));
    }

    @Test
    public void failsValidationWhenExpectedClassAndPassedInAreDifferent() throws Exception {
        final String xml = wrapBeanDef(
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean class=\"com.amazon.SomeType\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n");
        final String sourceForNumberProcessor =
                "package com.amazon;\n" +
                "\n" +
                "import com.amazon.SomeOtherType;\n" +
                "\n" +
                "public class NumberProcessor {\n" +
                "    public NumberProcessor(SomeOtherType sometype) {}\n" +
                "}";
        final String sourceForSomeType =
                "package com.amazon;\n" +
                "\n" +
                "public class SomeType { }";
        final String sourceForSomeOtherType =
                "package com.amazon;\n" +
                "\n" +
                "public class SomeOtherType { }";
        repository.addSource(sourceForNumberProcessor);
        repository.addSource(sourceForSomeType);
        repository.addSource(sourceForSomeOtherType);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);
        final GuessedTypes types = resolver.validateAndResolve(bean);

        assertThat(types.isValid(), equalTo(false));
    }

    @Test
    public void passingSubclassAsAnArgumentPassesValidation() throws Exception {
        final String xml = wrapBeanDef(
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean class=\"com.amazon.SomeType\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n");
        final String sourceForBeanClass =
                "package com.amazon;\n" +
                "\n" +
                "import com.amazon.SomeOtherType;\n" +
                "\n" +
                "public class NumberProcessor {\n" +
                "    public NumberProcessor(SomeOtherType sometype) {}\n" +
                "}";
        final String sourceForActualArgument =
                "package com.amazon;\n" +
                "\n" +
                "import com.amazon.SomeOtherType;\n" +
                "\n" +
                "public class SomeType extends SomeOtherType { }";
        final String sourceForExpectedArgument =
                "package com.amazon;\n" +
                "\n" +
                "public class SomeOtherType { }";
        repository.addSource(sourceForBeanClass);
        repository.addSource(sourceForActualArgument);
        repository.addSource(sourceForExpectedArgument);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);
        final GuessedTypes types = resolver.validateAndResolve(bean);

        assertThat(types.isValid(), equalTo(true));
    }

    @Test
    public void passesValidationWhenExpectedClassAndPassedInHaveEqualTypeVariable() throws Exception {
        final String xml = wrapBeanDef(
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean id=\"oneArg\" class=\"com.amazon.SomeType\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n");
        final String sourceForNumberProcessor =
                "package com.amazon;\n" +
                "\n" +
                "public class NumberProcessor {\n" +
                "    public NumberProcessor(com.amazon.List<com.amazon.Number> numbers) {}\n" +
                "}";
        final String sourceForSomeType =
                "package com.amazon;\n" +
                "\n" +
                "public class SomeType extends com.amazon.List<com.amazon.Number> { }";
        final String sourceForList =
                "package com.amazon;\n" +
                "\n" +
                "public class List<T> { }";
        repository.addSource(sourceForNumberProcessor);
        repository.addSource(sourceForSomeType);
        repository.addSource(sourceForList);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);
        final GuessedTypes types = resolver.validateAndResolve(bean);

        assertThat(types.isValid(), equalTo(true));
    }

    @Test
    public void failsValidationWhenExpectedClassAndPassedInHaveContravariantTypes() throws Exception {
        final String xml = wrapBeanDef(
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean id=\"oneArg\" class=\"com.amazon.SomeType\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n");
        final String sourceForNumberProcessor =
                "package com.amazon;\n" +
                "\n" +
                "public class NumberProcessor {\n" +
                "    public NumberProcessor(com.amazon.List<com.amazon.Number> numbers) {}\n" +
                "}";
        final String sourceForSomeType =
                "package com.amazon;\n" +
                "\n" +
                "public class SomeType extends com.amazon.List<com.amazon.List> { }";
        final String sourceForList =
                "package com.amazon;\n" +
                "\n" +
                "public class List<T> { }";
        repository.addSource(sourceForNumberProcessor);
        repository.addSource(sourceForSomeType);
        repository.addSource(sourceForList);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);
        final GuessedTypes types = resolver.validateAndResolve(bean);

        assertThat(types.isValid(), equalTo(false));
    }

    @Test
    public void passesValidationForCovariantTypes() throws Exception {
        final String xml = wrapBeanDef(
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean id=\"oneArg\" class=\"com.amazon.ListOfDoubles\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n");
        final String sourceForNumberProcessor =
                "package com.amazon;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class NumberProcessor<T extends com.amazon.Number> {\n" +
                "    public NumberProcessor(List<T> numbers) {}\n" +
                "}";
        final String sourceForListOfDoubles =
                "package com.amazon;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class ListOfDoubles implements List<com.amazon.Double> { }";
        final String sourceForList =
                "package java.util;\n" +
                "\n" +
                "public class List<T> {}";
        final String sourceForNumber =
                "package com.amazon;\n" +
                "\n" +
                "public class Number {}";
        final String sourceForDouble =
                "package com.amazon;\n" +
                "\n" +
                "public class Double extends com.amazon.Number {}";
        repository.addSource(sourceForNumberProcessor);
        repository.addSource(sourceForListOfDoubles);
        repository.addSource(sourceForList);
        repository.addSource(sourceForNumber);
        repository.addSource(sourceForDouble);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);

        final GuessedTypes types = resolver.validateAndResolve(bean);
        assertThat(types.isValid(), equalTo(true));
    }

    @Test
    public void failsValidationForInvariantTypes() throws Exception {
        final String xml = wrapBeanDef(
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean id=\"oneArg\" class=\"com.amazon.ListOfDoubles\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n");
        final String sourceForNumberProcessor =
                "package com.amazon;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class NumberProcessor<T extends com.amazon.Number> {\n" +
                "    public NumberProcessor(List<T> numbers) {}\n" +
                "}";
        final String sourceForListOfDoubles =
                "package com.amazon;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class ListOfDoubles implements List<com.amazon.Double> { }";
        final String sourceForList =
                "package java.util;\n" +
                "\n" +
                "public class List<T> {}";
        final String sourceForNumber =
                "package com.amazon;\n" +
                "\n" +
                "public class Number {}";
        final String sourceForDouble =
                "package com.amazon;\n" +
                "\n" +
                "public class Double extends java.lang.Object {}";
        repository.addSource(sourceForNumberProcessor);
        repository.addSource(sourceForListOfDoubles);
        repository.addSource(sourceForList);
        repository.addSource(sourceForNumber);
        repository.addSource(sourceForDouble);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);

        final GuessedTypes types = resolver.validateAndResolve(bean);
        assertThat(types.isValid(), equalTo(false));
    }

    @Test
    public void passesValidationAndResolvesAmbiguityWithOneGenericTypeParameter() throws Exception {
        final String xml = wrapBeanDef(
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                        "    <constructor-arg name=\"one\">\n" +
                        "        <bean class=\"com.amazon.ListOfDoubles\" />\n" +
                        "    </constructor-arg>\n" +
                        "</bean>\n");
        final String sourceForNumberProcessor =
                "package com.amazon;\n" +
                        "import java.util.List;\n" +
                        "\n" +
                        "public class NumberProcessor<T extends com.amazon.Number> {\n" +
                        "    public NumberProcessor(List<T> numbers) {}\n" +
                        "}";
        final String sourceForListOfDoubles =
                "package com.amazon;\n" +
                        "import java.util.List;\n" +
                        "\n" +
                        "public class ListOfDoubles<T extends com.amazon.Double> implements List<T> { }";
        final String sourceForList = "package java.util; public class List<T> {}";
        final String sourceForNumber = "package com.amazon; public class Number {}";
        final String sourceForDouble = "package com.amazon; public class Double extends com.amazon.Number {}";
        repository.addSource(sourceForNumberProcessor);
        repository.addSource(sourceForListOfDoubles);
        repository.addSource(sourceForList);
        repository.addSource(sourceForNumber);
        repository.addSource(sourceForDouble);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);

        final GuessedTypes types = resolver.validateAndResolve(bean);
        assertThat(types.isValid(), equalTo(true));

        final GuessedTypeParameters arg = types.getTypesFor(bean);
        assertThat(arg.getFqcnFor("T"), equalTo("com.amazon.Double"));
    }

    @Test
    public void resolvesTypeParameterForBuiltinSpringsUtilList() throws Exception {
        final String xml = wrapBeanDef(
                "<bean id=\"someId\" class=\"com.amazon.Component\">\n" +
                        "    <constructor-arg name=\"arg\">\n" +
                        "        <util:list>\n" +
                        "            <bean class=\"com.amazon.Orange\"/>\n" +
                        "            <bean class=\"com.amazon.Apple\"/>\n" +
                        "        </util:list>\n" +
                        "    </constructor-arg>\n" +
                        "</bean>");
        final String sourceForComponent =
                "package com.amazon;\n" +
                "import java.util.List;\n" +
                "public class Component<T> {\n" +
                "    public Component(List<T> items) {}\n" +
                "}";
        final String sourceForFruit = "package com.amazon; public class Fruit { }";
        final String sourceForApple = "package com.amazon; public class Apple extends com.amazon.Fruit {}";
        final String sourceForOrange = "package com.amazon; public class Orange extends com.amazon.Fruit {}";
        repository.addSource(sourceForComponent);
        repository.addSource(sourceForFruit);
        repository.addSource(sourceForApple);
        repository.addSource(sourceForOrange);

        repository.addSource(
                "package org.springframework.beans.factory.config;\n" +
                "import java.util.List; \n" +
                "public class ListFactoryBean<T> implements List<T> {\n" +
                "    public ListFactoryBean(T one, T two) {}" +
                "}"
        );
        repository.addSource(
                "package java.util; public class List<T> {}"
        );

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);

        final GuessedTypes types = resolver.validateAndResolve(bean);
        assertThat(types.isValid(), equalTo(true));

        final GuessedTypeParameters arg = types.getTypesFor(bean);
        assertThat(arg.getFqcnFor("T"), equalTo("com.amazon.Fruit"));
    }

}
