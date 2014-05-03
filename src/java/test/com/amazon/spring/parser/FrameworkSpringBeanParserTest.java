package com.amazon.spring.parser;

import com.amazon.spring.BeanDefinition;
import com.amazon.spring.ConstructorArgument;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class FrameworkSpringBeanParserTest {

    private static final String SIMPLE_BEAN = wrapBeanDef(
            "<bean id=\"oneArg\" class=\"com.amazon.NumberProcessor\">\n" +
            "    <constructor-arg name=\"one\">\n" +
            "        <bean class=\"com.amazon.ListOfDoubles\" />\n" +
            "    </constructor-arg>\n" +
            "</bean>");

    private static final String BUNCH_OF_ARGS = wrapBeanDef(
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
            "</bean>");

    public static final String SOME_LISTS = wrapBeanDef(
            "<bean id=\"someId\" class=\"com.amazon.Component\">\n" +
            "    <constructor-arg name=\"arg\">\n" +
            "        <util:list>\n" +
            "            <ref bean=\"content1\"/>\n" +
            "            <ref bean=\"content2\"/>\n" +
            "        </util:list>\n" +
            "    </constructor-arg>\n" +
            "</bean>\n"
    );

    private final SpringBeanParser parser = new FrameworkSpringBeanParser();

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

    @Test
    public void extractsBeanWithUtilListArgument() throws Exception {
        final BeanDefinition bean = parser.parse(SOME_LISTS);

        assertThat(bean.getConstructorArgs(), hasSize(1));

        final ConstructorArgument argBean = bean.getConstructorArgs().get(0);
        assertThat(argBean.getName(), equalTo("arg"));
        assertThat(argBean.getBeanDefinition().getFqcn(), equalTo("java.utill.List"));
    }

    public static String wrapBeanDef(String beanDefXml) {
        return
                "<?xml version=\"1.0\" ?>\n" +
                "<beans\n" +
                "        xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xmlns:util=\"http://www.springframework.org/schema/util\"\n" +
                "        xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd\n" +
                        "                           http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.1.xsd\">"
                + beanDefXml
                + "</beans>";
    }
}
