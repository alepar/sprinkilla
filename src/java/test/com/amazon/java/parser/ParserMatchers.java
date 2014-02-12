package com.amazon.java.parser;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import com.amazon.java.TypeDefinition;

public class ParserMatchers {

    public static Matcher<TypeDefinition> isSimpleFqcnType(final String fqcn) {
        return new BaseMatcher<TypeDefinition>() {
            @Override
            public boolean matches(Object item) {
                if (! (item instanceof TypeDefinition)) {
                    return false;
                }

                final TypeDefinition type = (TypeDefinition) item;
                return fqcn.equals(type.getFqcn()) && type.getGenericArguments().size() == 0 && type.getGenericParam() == null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("type definition with fqcn").appendValue(fqcn);
            }
        };
    }

}
