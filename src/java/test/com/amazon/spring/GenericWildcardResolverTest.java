package com.amazon.spring;

import java.io.StringReader;

import org.junit.Test;

import com.amazon.java.MapDefinitionProvider;
import com.amazon.java.parser.JavaParser;
import com.amazon.java.parser.antlr.AntlrJavaParser;
import com.amazon.spring.parser.SpringBeanParser;
import com.amazon.spring.parser.XercesSpringBeanParser;
import com.amazon.spring.resolver.GenericWildcardResolver;
import com.amazon.spring.resolver.ResolvedTypes;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class GenericWildcardResolverTest {

    private final SpringBeanParser springBeanParser = new XercesSpringBeanParser();
    private final JavaParser javaParser = new AntlrJavaParser();

    @Test
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
        final MapDefinitionProvider defProvider = new MapDefinitionProvider();
        defProvider.addDefinition(javaParser.parse(new StringReader(sourceForNumberProcessor)));
        defProvider.addDefinition(javaParser.parse(new StringReader(sourceForListOfDoubles)));

        final BeanDefinition bean = springBeanParser.parse(xml);
        final GenericWildcardResolver resolver = new GenericWildcardResolver(defProvider);
        final ResolvedTypes types = resolver.resolve(bean);

        final ResolvedArguments arg = types.getTypesFor(bean);

        assertThat(arg.getTypeFor("T"), equalTo("Double"));
    }
}
