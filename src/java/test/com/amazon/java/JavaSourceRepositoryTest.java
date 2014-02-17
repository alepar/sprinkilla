package com.amazon.java;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.amazon.java.parser.antlr.AntlrJavaSourceParser;
import com.amazon.java.parser.antlr.AntlrTypeDefinition;
import com.amazon.java.parser.antlr.AntlrTypeParameter;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class JavaSourceRepositoryTest {

    private final JavaSourceRepository repository = new JavaSourceRepository(new MapDefinitionProvider(), new AntlrJavaSourceParser());

    @Test
    public void covariantTypesAreAssignable() throws Exception {
        final String listClass = "public class List<T> {}";
        final String doubleClass = "public class Double extends Number {}";
        final String numberClass = "public class Number {}";

        repository.addSource(listClass);
        repository.addSource(doubleClass);
        repository.addSource(numberClass);

        final TypeDefinition listOfDoublesType = new AntlrTypeDefinition(
                "List",
                Arrays.<TypeDefinition>asList(new AntlrTypeDefinition(
                        "Double",
                        Collections.<TypeDefinition>emptyList(),
                        null
                )),
                null
        );

        final TypeDefinition listOfSomeNumbersType =  new AntlrTypeDefinition(
                "List",
                Arrays.<TypeDefinition>asList(new AntlrTypeDefinition(
                        null,
                        Collections.<TypeDefinition>emptyList(),
                        new AntlrTypeParameter(
                            "T", TypeParameter.BoundaryModifier.EXTENDS,
                            new AntlrTypeDefinition("Number", Collections.<TypeDefinition>emptyList(), null),
                            null
                        )
                )),
                null
        );

        assertThat(repository.isAssignable(listOfDoublesType, listOfSomeNumbersType), equalTo(true));
    }

    @Test
    public void invariantWhenSourceTypeParameterDoesNotMeetDestinationExtendsBoundary() throws Exception {
        final String listClass = "public class List<T> {}";
        final String doubleClass = "public class String {}";
        final String numberClass = "public class Number {}";

        repository.addSource(listClass);
        repository.addSource(doubleClass);
        repository.addSource(numberClass);


        final TypeDefinition listOfStringsType = new AntlrTypeDefinition(
                "List",
                Arrays.<TypeDefinition>asList(new AntlrTypeDefinition(
                        "String",
                        Collections.<TypeDefinition>emptyList(),
                        null
                )),
                null
        );

        final TypeDefinition listOfSomeNumbersType =  new AntlrTypeDefinition(
                "List",
                Arrays.<TypeDefinition>asList(new AntlrTypeDefinition(
                        null,
                        Collections.<TypeDefinition>emptyList(),
                        new AntlrTypeParameter(
                            "T", TypeParameter.BoundaryModifier.EXTENDS,
                            new AntlrTypeDefinition("Number", Collections.<TypeDefinition>emptyList(), null),
                            null
                        )
                )),
                null
        );

        assertThat(repository.isAssignable(listOfStringsType, listOfSomeNumbersType), equalTo(false));
    }

    @Test
    public void invariantWhenSourceTypeParameterIsSubclassOfDestinationTypeParameter() throws Exception {
        final String listClass = "public class List<T> {}";
        final String doubleClass = "public class Double extends Number {}";
        final String numberClass = "public class Number {}";

        repository.addSource(listClass);
        repository.addSource(doubleClass);
        repository.addSource(numberClass);


        final TypeDefinition listOfDoublesType = new AntlrTypeDefinition(
                "List",
                Arrays.<TypeDefinition>asList(new AntlrTypeDefinition(
                        "Double",
                        Collections.<TypeDefinition>emptyList(),
                        null
                )),
                null
        );

        final TypeDefinition listOfNumbersType = new AntlrTypeDefinition(
                "List",
                Arrays.<TypeDefinition>asList(new AntlrTypeDefinition(
                        "Number",
                        Collections.<TypeDefinition>emptyList(),
                        null
                )),
                null
        );

        assertThat(repository.isAssignable(listOfDoublesType, listOfNumbersType), equalTo(false));
    }
}
