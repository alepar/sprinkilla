package com.amazon.spring.parser;

import org.junit.Test;

import com.amazon.spring.BeanDefinition;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class XercesSpringBeanParserTest {

    private static final String SIMPLE_BEAN =
            "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
            "    <constructor-arg name=\"one\">\n" +
            "        <bean id=\"oneArg\" class=\"com.amazon.ListOfDoubles\" />\n" +
            "    </constructor-arg>\n" +
            "</bean>";

    private final SpringBeanParser parser = new XercesSpringBeanParser();

    @Test
    public void extractsNameProperly() throws Exception {
        final BeanDefinition bean = parser.parse(SIMPLE_BEAN);

        assertThat(bean.getName(), equalTo("oneArg"));
    }
}
