package com.amazon.spring.parser;

import com.amazon.spring.BeanDefinition;
import com.amazon.spring.ConstructorArgument;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XercesSpringBeanParser implements SpringBeanParser {

    private final XMLInputFactory inputFactory;

    public XercesSpringBeanParser() {
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
    }

    @Override
    public BeanDefinition parse(String xml) {
        try {
            final XMLStreamReader xr = inputFactory.createXMLStreamReader(new StringReader(xml));

            while(xr.hasNext()) {
                final int next = xr.next();
                if (next == XMLStreamConstants.START_ELEMENT && xr.getLocalName().equals("bean")) {
                    return recurse(xr);
                }
            }

            throw new RuntimeException("couldn't find bean definitions");
        } catch (XMLStreamException e) {
            throw new RuntimeException("failed to parse xml", e);
        }
    }

    private static XercesBeanDefinition recurse(XMLStreamReader xr) throws XMLStreamException {
        final XercesBeanDefinition result = new XercesBeanDefinition();

        result.name = xr.getAttributeValue(null, "id");
        result.classname = xr.getAttributeValue(null, "class");
        State state = State.WAITFOR_ARGS;

        while (xr.hasNext()) {
            final int next = xr.next();
            if (next == XMLStreamConstants.START_ELEMENT) {
                if (xr.getLocalName().equals("constructor-arg")) {
                    final String argName = xr.getAttributeValue(null, "name");

                    while(!xr.isStartElement() || !xr.getLocalName().equals("bean")) {
                        if(!xr.hasNext()) {
                            throw new RuntimeException("constructor-arg should be defined as bean: " + argName);
                        }
                        xr.next();
                    }

                    result.constructorArgs.add(new XercesConstructorArgument(
                            argName,
                            recurse(xr)
                    ));
                }
            } else if (next == XMLStreamConstants.END_ELEMENT) {
                if (state == State.WAITFOR_ARGS && xr.getLocalName().equals("bean") && xr.isEndElement()) {
                    return result;
                }
            } 
        }

        throw new RuntimeException("unfinished bean definition: " + result.getName());
    }

    private static enum State {
        WAITFOR_ARGS
    }

    private static class XercesBeanDefinition implements BeanDefinition {
        private final List<ConstructorArgument> constructorArgs = new ArrayList<>();
        private String name;
        private String classname;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getFqcn() {
            return classname;
        }

        @Override
        public List<ConstructorArgument> getConstructorArgs() {
            return constructorArgs;
        }
    }

    private static class XercesConstructorArgument implements ConstructorArgument {
        private final String name;
        private final XercesBeanDefinition beanDefinition;

        public XercesConstructorArgument(String name, XercesBeanDefinition beanDefinition) {
            this.name = name;
            this.beanDefinition = beanDefinition;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public BeanDefinition getBeanDefinition() {
            return beanDefinition;
        }
    }
}
