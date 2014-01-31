package com.amazon.parser.java.antlr;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import com.amazon.parser.java.ClassDefinition;

import static com.amazon.parser.java.GenericArgument.BoundaryType.EXTENDS;
import static com.amazon.parser.java.GenericArgument.BoundaryType.NONE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AntlrJavaParserTest {

    private final com.amazon.parser.java.JavaParser parser = new AntlrJavaParser();

    @Test
    public void extractsClassNameRight() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname {}"));

        assertThat(definition.getName(), equalTo("Testname"));
        assertThat(definition.getGenericArguments(), hasSize(0));
    }

    @Test
    public void extractsSingleGenericTypeParameterRight() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname<T> {}"));

        assertThat(definition.getGenericArguments(), hasSize(1));
        assertThat(definition.getGenericArguments().get(0).getName(), equalTo("T"));
        assertThat(definition.getGenericArguments().get(0).getBoundaryType(), equalTo(NONE));
        assertThat(definition.getGenericArguments().get(0).getName(), equalTo("T"));
    }

    @Test
    public void extractsTwoGenericTypeParameterRight() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname<T, E> {}"));

        assertThat(definition.getGenericArguments(), hasSize(2));
        assertThat(definition.getGenericArguments().get(0).getName(), equalTo("T"));
        assertThat(definition.getGenericArguments().get(1).getName(), equalTo("E"));
    }

    @Test
    public void extractsBoundParameter() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname<T extends Number> {}"));

        assertThat(definition.getGenericArguments(), hasSize(1));
        assertThat(definition.getGenericArguments().get(0).getName(), equalTo("T"));
        assertThat(definition.getGenericArguments().get(0).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getGenericArguments().get(0).getBoundaryName(), equalTo("Number"));
    }

    @Test
    public void returnsQualifiedBoundaryNamesWhereAvailable() throws Exception {
        final ClassDefinition definition = parser.parse(from("import java.util.Number;\npublic class Testname<T extends Number> {}"));

        assertThat(definition.getGenericArguments(), hasSize(1));
        assertThat(definition.getGenericArguments().get(0).getName(), equalTo("T"));
        assertThat(definition.getGenericArguments().get(0).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getGenericArguments().get(0).getBoundaryName(), equalTo("java.util.Number"));
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

        assertThat(definition.getGenericArguments(), hasSize(5));
        assertThat(definition.getGenericArguments().get(0).getName(), equalTo("R"));
        assertThat(definition.getGenericArguments().get(0).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getGenericArguments().get(0).getBoundaryName(), equalTo("com.amazon.fenix.client.v2.DetailPageRequest"));

        assertThat(definition.getGenericArguments().get(1).getName(), equalTo("P"));
        assertThat(definition.getGenericArguments().get(1).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getGenericArguments().get(1).getBoundaryName(), equalTo("com.amazon.fenix.client.v2.ProductAdsProgram"));

        assertThat(definition.getGenericArguments().get(2).getName(), equalTo("RD"));
        assertThat(definition.getGenericArguments().get(2).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getGenericArguments().get(2).getBoundaryName(), equalTo("com.amazon.fenix.v2.ProductAdsRequestDecoration"));

        assertThat(definition.getGenericArguments().get(3).getName(), equalTo("A"));
        assertThat(definition.getGenericArguments().get(3).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getGenericArguments().get(3).getBoundaryName(), equalTo("com.amazon.fenix.model.ad.Ad"));

        assertThat(definition.getGenericArguments().get(4).getName(), equalTo("D"));
        assertThat(definition.getGenericArguments().get(4).getBoundaryType(), equalTo(EXTENDS));
        assertThat(definition.getGenericArguments().get(4).getBoundaryName(), equalTo("com.amazon.fenix.model.decorationentity.SponsoredOfferListingDecorationEntity"));
    }

    private static Reader from(String s) {
        return new StringReader(s);
    }
}
