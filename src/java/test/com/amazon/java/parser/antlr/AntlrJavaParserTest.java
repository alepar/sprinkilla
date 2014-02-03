package com.amazon.java.parser.antlr;

import com.amazon.java.ClassDefinition;
import com.amazon.java.GenericParameter;
import com.amazon.java.MethodDefinition;
import com.amazon.java.Variable;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

import static com.amazon.java.GenericParameter.BoundaryType.EXTENDS;
import static com.amazon.java.GenericParameter.BoundaryType.NO_WILDCARD;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AntlrJavaParserTest {

    private final com.amazon.java.parser.JavaParser parser = new AntlrJavaParser();

    @Test
    public void extractsClassNameRight() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname {}"));

        assertThat(definition.getType().getFqcn(), equalTo("Testname"));
        assertThat(definition.getType().getGenericParameters(), hasSize(0));
    }

    @Test
    public void extractsSingleGenericTypeParameterRight() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname<T> {}"));

        assertThat(definition.getType().getGenericParameters(), hasSize(1));
        assertThat(definition.getType().getGenericParameters().get(0).getName(), equalTo("T"));
        assertThat(definition.getType().getGenericParameters().get(0).getBoundaryType(), equalTo(NO_WILDCARD));
        assertThat(definition.getType().getGenericParameters().get(0).getName(), equalTo("T"));
    }

    @Test
    public void extractsTwoGenericTypeParameterRight() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname<T, E> {}"));

        assertThat(definition.getType().getGenericParameters(), hasSize(2));
        assertThat(definition.getType().getGenericParameters().get(0).getName(), equalTo("T"));
        assertThat(definition.getType().getGenericParameters().get(1).getName(), equalTo("E"));
    }

    @Test
    public void extractsBoundParameter() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname<T extends Number> {}"));

        assertThat(definition.getType().getGenericParameters(), hasSize(1));
        assertThat(definition.getType().getGenericParameters().get(0).getName(), equalTo("T"));
        assertThat(definition.getType().getGenericParameters().get(0).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericParameters().get(0).getBoundaryFqcn(), equalTo("Number"));
    }

    @Test
    public void returnsQualifiedBoundaryNamesWhereAvailable() throws Exception {
        final ClassDefinition definition = parser.parse(from("import java.util.Number;\npublic class Testname<T extends Number> {}"));

        assertThat(definition.getType().getGenericParameters(), hasSize(1));
        assertThat(definition.getType().getGenericParameters().get(0).getName(), equalTo("T"));
        assertThat(definition.getType().getGenericParameters().get(0).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericParameters().get(0).getBoundaryFqcn(), equalTo("java.util.Number"));
    }

    @Test
    public void returnsProperlyBuiltFqcnWhenClassIsInsideSomePackage() throws Exception {
        final ClassDefinition definition = parser.parse(from("package com.amazon;\n\npublic class SomeName {}"));

        assertThat(definition.getType().getFqcn(), equalTo("com.amazon.SomeName"));
        assertThat(definition.getType().getGenericParameters(), hasSize(0));
    }

    @Test
    public void realWorldExampleParsedCorrectly() throws Exception {
        final String source =
            "package com.amazon.fenix.component.filter.predicate;\n" +
            "\n" +
            "import com.amazon.fenix.client.v2.DetailPageRequest;\n" +
            "import com.amazon.fenix.client.v2.ProductAdsProgram;\n" +
            "import com.amazon.fenix.model.ad.Ad;\n" +
            "import com.amazon.fenix.model.decorationentity.SponsoredOfferListingDecorationEntity;\n" +
            "import com.amazon.fenix.v2.ProductAdsRequestDecoration;\n" +
            "\n" +
            "/**\n" +
            " * This filter removes ads based on the browse ladder of the page ASIN and the browse ladder of the ad\n" +
            " */\n" +
            "public class BrowseNodeFilter<R extends DetailPageRequest, P extends ProductAdsProgram, RD extends ProductAdsRequestDecoration, A extends Ad, D extends SponsoredOfferListingDecorationEntity> implements AdProviderComponent<R, P, RD, A, D> {\n" +
            "\n" +
            "}\n";
        final ClassDefinition definition = parser.parse(from(source));

        assertThat(definition.getType().getGenericParameters(), hasSize(5));
        assertThat(definition.getType().getGenericParameters().get(0).getName(), equalTo("R"));
        assertThat(definition.getType().getGenericParameters().get(0).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericParameters().get(0).getBoundaryFqcn(), equalTo("com.amazon.fenix.client.v2.DetailPageRequest"));

        assertThat(definition.getType().getGenericParameters().get(1).getName(), equalTo("P"));
        assertThat(definition.getType().getGenericParameters().get(1).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericParameters().get(1).getBoundaryFqcn(), equalTo("com.amazon.fenix.client.v2.ProductAdsProgram"));

        assertThat(definition.getType().getGenericParameters().get(2).getName(), equalTo("RD"));
        assertThat(definition.getType().getGenericParameters().get(2).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericParameters().get(2).getBoundaryFqcn(), equalTo("com.amazon.fenix.v2.ProductAdsRequestDecoration"));

        assertThat(definition.getType().getGenericParameters().get(3).getName(), equalTo("A"));
        assertThat(definition.getType().getGenericParameters().get(3).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericParameters().get(3).getBoundaryFqcn(), equalTo("com.amazon.fenix.model.ad.Ad"));

        assertThat(definition.getType().getGenericParameters().get(4).getName(), equalTo("D"));
        assertThat(definition.getType().getGenericParameters().get(4).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericParameters().get(4).getBoundaryFqcn(), equalTo("com.amazon.fenix.model.decorationentity.SponsoredOfferListingDecorationEntity"));
    }

    @Test
    public void extractsSimpleOneArgConstructorRight() throws Exception {
        final ClassDefinition definition = parser.parse(from(
                "import com.amazon.Number;\n" +
                "\n" +
                "public class Testname {\n" +
                "\n" +
                "\tpublic Testname(Number arg) { }\n" +
                "\n" +
                "}"
        ));

        assertThat(definition.getConstructors(), hasSize(1));

        final MethodDefinition constructor = definition.getConstructors().get(0);
        assertThat(constructor.getName(), equalTo("Testname"));
        assertThat(constructor.getArguments(), hasSize(1));

        final Variable constructorArg = constructor.getArguments().get(0);
        assertThat(constructorArg.getType().getFqcn(), equalTo("com.amazon.Number"));
        assertThat(constructorArg.getType().getGenericParameters(), hasSize(0));
        assertThat(constructorArg.getName(), equalTo("arg"));
    }

    @Test
    public void extractsConstructorWithOneGenericTypeArgumentRight() throws Exception {
        final ClassDefinition definition = parser.parse(from(
                "import com.amazon.Number;\n" +
                "import com.amazon.List;\n" +
                "\n" +
                "public class Testname {\n" +
                "\n" +
                "\tpublic Testname(List<Number> arg) { }\n" +
                "\n" +
                "}"
        ));

        final MethodDefinition constructor = definition.getConstructors().get(0);
        final Variable constructorArg = constructor.getArguments().get(0);
        assertThat(constructorArg.getType().getFqcn(), equalTo("com.amazon.List"));
        assertThat(constructorArg.getType().getGenericParameters(), hasSize(1));

        final GenericParameter genericParameter = constructorArg.getType().getGenericParameters().get(0);
        assertThat(genericParameter.getBoundaryType(), equalTo(NO_WILDCARD));
        assertThat(genericParameter.getBoundaryFqcn(), equalTo("com.amazon.Number"));
    }

    @Test
    public void extractsConstructorWithOneGenericWildcardedTypeArgumentRight() throws Exception {
        final ClassDefinition definition = parser.parse(from(
                "import com.amazon.Number;\n" +
                "import com.amazon.List;\n" +
                "\n" +
                "public class Testname<T extends Number> {\n" +
                "\n" +
                "\tpublic Testname(List<T> arg) { }\n" +
                "\n" +
                "}"
        ));

        final MethodDefinition constructor = definition.getConstructors().get(0);
        final Variable constructorArg = constructor.getArguments().get(0);
        final GenericParameter genericParameter = constructorArg.getType().getGenericParameters().get(0);
        assertThat(genericParameter.getName(), equalTo("T"));
        assertThat(genericParameter.getBoundaryType(), equalTo(EXTENDS));
        assertThat(genericParameter.getBoundaryFqcn(), equalTo("com.amazon.Number"));
    }

    private static Reader from(String s) {
        return new StringReader(s);
    }
}
