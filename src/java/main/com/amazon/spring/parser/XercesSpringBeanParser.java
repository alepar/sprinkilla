package com.amazon.spring.parser;

import com.amazon.spring.BeanDefinition;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
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
            final BeanDefinitionImpl result = new BeanDefinitionImpl();

            State state = State.WAITFORTAG_BEAN;
            while (xr.hasNext()) {
                final int next = xr.next();
                if (next == XMLStreamConstants.START_ELEMENT) {
                    if (xr.getLocalName().equals("bean")) {
                        result.name = xr.getAttributeValue(null, "id");
                        result.classname = xr.getAttributeValue(null, "class");
                    }
                }
            }

            return result;
        } catch (XMLStreamException e) {
            throw new RuntimeException("failed to parse xml", e);
        }
    }

    private static enum State {
        WAITFORTAG_BEAN
    }

    private static class BeanDefinitionImpl implements BeanDefinition {
        private String name;
        private String classname;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getClassname() {
            return classname;
        }

        @Override
        public List<BeanDefinition> getConstructorArgs() {
            throw new RuntimeException("parfenov, implement me!");
        }
    }
}
