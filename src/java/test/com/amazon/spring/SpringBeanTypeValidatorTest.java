package com.amazon.spring;

import com.amazon.java.IndexedTypeHierarchy;
import com.amazon.java.JavaSourceRepository;
import com.amazon.java.MapDefinitionProvider;
import com.amazon.java.parser.antlr.AntlrJavaSourceParser;
import com.amazon.spring.parser.SpringBeanParser;
import com.amazon.spring.parser.XercesSpringBeanParser;
import com.amazon.spring.resolver.ResolvedTypes;
import com.amazon.spring.resolver.SpringBeanTypeValidator;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SpringBeanTypeValidatorTest {

    private final JavaSourceRepository repository = new JavaSourceRepository(new IndexedTypeHierarchy(), new MapDefinitionProvider(), new AntlrJavaSourceParser());

    private final SpringBeanParser springBeanParser = new XercesSpringBeanParser();

    @Test
    public void passesValidationWhenExpectedClassAndPassedInAreTheSame() throws Exception {
        final String xml =
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean id=\"oneArg\" class=\"com.amazon.SomeType\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n";
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
        final ResolvedTypes types = resolver.validateAndResolve(bean);

        assertThat(types.isValid(), equalTo(true));
    }

    @Test
    public void failsValidationWhenExpectedClassAndPassedInAreDifferent() throws Exception {
        final String xml =
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean class=\"com.amazon.SomeType\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n";
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
        final ResolvedTypes types = resolver.validateAndResolve(bean);

        assertThat(types.isValid(), equalTo(false));
    }

    @Test
    public void passingSubclassAsAnArgumentPassesValidation() throws Exception {
        final String xml =
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean class=\"com.amazon.SomeType\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n";
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
        final ResolvedTypes types = resolver.validateAndResolve(bean);

        assertThat(types.isValid(), equalTo(true));
    }

    @Test
    public void passesValidationWhenExpectedClassAndPassedInHaveEqualTypeVariable() throws Exception {
        final String xml =
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean id=\"oneArg\" class=\"com.amazon.SomeType\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n";
        final String sourceForNumberProcessor =
                "package com.amazon;\n" +
                "\n" +
                "public class NumberProcessor {\n" +
                "    public NumberProcessor(com.amazon.List<com.amazon.Number> numbers) {}\n" +
                "}";
        final String sourceForListOfDoubles =
                "package com.amazon;\n" +
                "\n" +
                "public class SomeType extends com.amazon.List<com.amazon.Number> { }";
        repository.addSource(sourceForNumberProcessor);
        repository.addSource(sourceForListOfDoubles);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);
        final ResolvedTypes types = resolver.validateAndResolve(bean);

        assertThat(types.isValid(), equalTo(true));
    }

    @Test @Ignore
    public void resolvesExampleWithOneArgument() throws Exception {
        final String xml =
                "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
                "    <constructor-arg name=\"one\">\n" +
                "        <bean id=\"oneArg\" class=\"com.amazon.ListOfDoubles\" />\n" +
                "    </constructor-arg>\n" +
                "</bean>\n";
        final String sourceForNumberProcessor =
                "package com.amazon;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class NumberProcessor<T extends Number> {\n" +
                "    public NumberProcessor(List<T> numbers) {}\n" +
                "}";
        final String sourceForListOfDoubles =
                "package com.amazon;\n" +
                "\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class ListOfDoubles<T extends Double> implements List<T> { }";
        repository.addSource(sourceForNumberProcessor);
        repository.addSource(sourceForListOfDoubles);

        final BeanDefinition bean = springBeanParser.parse(xml);
        final SpringBeanTypeValidator resolver = new SpringBeanTypeValidator(repository, repository);
        final ResolvedTypes types = resolver.validateAndResolve(bean);

        final ResolvedArguments arg = types.getTypesFor(bean);

        assertThat(arg.getTypeFor("T"), equalTo("Double"));
    }
}
