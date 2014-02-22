package com.amazon.spring.parser;

import org.junit.Test;

import com.amazon.spring.BeanDefinition;
import com.amazon.spring.ConstructorArgument;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class XercesSpringBeanParserTest {

    <bean id="someId" class="com.amazon.Component">
        <constructor-arg name="name" value="nameValue"/>
        <constructor-arg name="component">
            <bean class="com.amazon.AnotherComponent">
                <constructor-arg name="contents">
                    <util:list>
                        <ref bean="content1"/>
                        <ref bean="content2"/>
                    </util:list>
                </constructor-arg>
            </bean>
        </constructor-arg>
    </bean>

    private static final String SIMPLE_BEAN =
            "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
            "    <constructor-arg name=\"one\">\n" +
            "        <bean class=\"com.amazon.ListOfDoubles\" />\n" +
            "    </constructor-arg>\n" +
            "</bean>";

    private static final String BUNCH_OF_ARGS =
            "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
            "    <constructor-arg name=\"one\">\n" +
            "        <bean class=\"com.amazon.First\" />\n" +
            "    </constructor-arg>\n" +
            "    <constructor-arg name=\"two\">\n" +
            "        <bean class=\"com.amazon.Second\" />\n" +
            "    </constructor-arg>\n" +
            "    <constructor-arg name=\"three\">\n" +
            "        <bean class=\"com.amazon.Third\" />\n" +
            "    </constructor-arg>\n" +
            "</bean>";


    private final SpringBeanParser parser = new XercesSpringBeanParser();

    @Test
    public void extractsNameProperly() throws Exception {
        final BeanDefinition bean = parser.parse(SIMPLE_BEAN);

        assertThat(bean.getName(), equalTo("oneArg"));
        assertThat(bean.getFqcn(), equalTo("com.amazon.NumberProcessor"));
    }

    @Test
    public void extractsArgumentBeanCorrectly() throws Exception {
        final BeanDefinition bean = parser.parse(SIMPLE_BEAN);
        assertThat(bean.getConstructorArgs(), hasSize(1));

        final ConstructorArgument argBean = bean.getConstructorArgs().get(0);
        assertThat(argBean.getName(), equalTo("one"));
        assertThat(argBean.getBeanDefinition().getFqcn(), equalTo("com.amazon.ListOfDoubles"));
    }

    @Test
    public void extractsMultipleArgConstructorsCorrectly() throws Exception {
        final BeanDefinition bean = parser.parse(BUNCH_OF_ARGS);
        assertThat(bean.getConstructorArgs(), hasSize(3));

        ConstructorArgument argBean;

        argBean = bean.getConstructorArgs().get(0);
        assertThat(argBean.getName(), equalTo("one"));
        assertThat(argBean.getBeanDefinition().getFqcn(), equalTo("com.amazon.First"));

        argBean = bean.getConstructorArgs().get(1);
        assertThat(argBean.getName(), equalTo("two"));
        assertThat(argBean.getBeanDefinition().getFqcn(), equalTo("com.amazon.Second"));

        argBean = bean.getConstructorArgs().get(2);
        assertThat(argBean.getName(), equalTo("three"));
        assertThat(argBean.getBeanDefinition().getFqcn(), equalTo("com.amazon.Third"));
    }
}
