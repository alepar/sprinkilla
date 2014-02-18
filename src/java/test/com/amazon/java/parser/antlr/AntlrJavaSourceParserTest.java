package com.amazon.java.parser.antlr;

import java.io.Reader;
import java.io.StringReader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.amazon.java.ClassDefinition;
import com.amazon.java.MethodDefinition;
import com.amazon.java.TypeDefinition;
import com.amazon.java.TypeParameter;
import com.amazon.java.TypeParameterContext;
import com.amazon.java.Variable;
import com.amazon.java.parser.JavaSourceParser;

import static com.amazon.java.TypeParameter.BoundaryModifier.EXTENDS;
import static com.amazon.java.parser.ParserMatchers.isSimpleFqcnType;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class AntlrJavaSourceParserTest {

    private final JavaSourceParser parser = new AntlrJavaSourceParser();

    @Test
    public void extractsClassNameRight() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname {}"));

        assertThat(definition.getType().getFqcn(), equalTo("Testname"));
        assertThat(definition.getType().getGenericTypeParameters(), hasSize(0));
    }

    @Test
    public void extractsSingleGenericTypeParameterRight() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname<T> {}"));

        assertThat(definition.getType().getGenericTypeParameters(), hasSize(1));

        final TypeDefinition typeDef = definition.getType().getGenericTypeParameters().get(0);
        assertThat(typeDef.getFqcn(), nullValue());
        assertThat(typeDef.getGenericTypeParameters(), hasSize(0));
        final TypeParameter param = typeDef.getTypeParameter();
        assertThat(param.getName(), equalTo("T"));

        final TypeParameterContext ctx = param.getContext();
        assertThat(ctx, notNullValue());
        assertThat(param, notNullValue());
        assertThat(param.getBoundaryModifier(), equalTo(EXTENDS));
        assertThat(param.getName(), equalTo("T"));
        final TypeDefinition boundaryType = param.getBoundaryType();
        assertThat(boundaryType, isSimpleFqcnType("java.lang.Object"));
    }

    @Test
    public void extractsTwoGenericTypeParameterRight() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname<T, E> {}"));

        assertThat(definition.getType().getGenericTypeParameters(), hasSize(2));

        assertThat(definition.getType().getGenericTypeParameters().get(0).getTypeParameter().getName(), equalTo("T"));
        assertThat(definition.getType().getGenericTypeParameters().get(1).getTypeParameter().getName(), equalTo("E"));
    }

    @Test
    public void extractsExtendsBoundTypeInGenericParameter() throws Exception {
        final ClassDefinition definition = parser.parse(from("public class Testname<T extends Number> {}"));

        assertThat(definition.getType().getGenericTypeParameters(), hasSize(1));

        final TypeDefinition typeDef = definition.getType().getGenericTypeParameters().get(0);
        assertThat(typeDef.getFqcn(), nullValue());
        assertThat(typeDef.getGenericTypeParameters(), hasSize(0));

        final TypeParameter param = typeDef.getTypeParameter();
        assertThat(typeDef.getTypeParameter().getName(), equalTo("T"));

        assertThat(param.getContext(), notNullValue());
        assertThat(param, notNullValue());
        assertThat(param.getBoundaryModifier(), equalTo(EXTENDS));
        assertThat(param.getName(), equalTo("T"));
        final TypeDefinition boundaryType = param.getBoundaryType();
        assertThat(boundaryType, isSimpleFqcnType("Number"));
    }

    @Test
    public void returnsQualifiedBoundaryNamesWhereAvailable() throws Exception {
        final ClassDefinition definition = parser.parse(from("import java.util.Number;\npublic class Testname<T extends Number> {}"));

        assertThat(definition.getType().getGenericTypeParameters(), hasSize(1));

        final TypeDefinition typeDef = definition.getType().getGenericTypeParameters().get(0);
        final TypeParameter param = typeDef.getTypeParameter();
        final TypeDefinition boundaryType = param.getBoundaryType();
        assertThat(boundaryType, isSimpleFqcnType("java.util.Number"));
    }

    @Test
    public void returnsProperlyBuiltFqcnWhenClassIsInsideSomePackage() throws Exception {
        final ClassDefinition definition = parser.parse(from("package com.amazon;\n\npublic class SomeName {}"));

        assertThat(definition.getType(), isSimpleFqcnType("com.amazon.SomeName"));
        assertThat(definition.getType().getGenericTypeParameters(), hasSize(0));
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

        assertThat(definition.getType().getGenericTypeParameters(), hasSize(5));
        assertThat(definition.getType().getGenericTypeParameters().get(0).getTypeParameter().getName(), equalTo("R"));
        assertThat(definition.getType().getGenericTypeParameters().get(0).getTypeParameter().getBoundaryModifier(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericTypeParameters().get(0).getTypeParameter().getBoundaryType(), isSimpleFqcnType("com.amazon.fenix.client.v2.DetailPageRequest"));

        assertThat(definition.getType().getGenericTypeParameters().get(1).getTypeParameter().getName(), equalTo("P"));
        assertThat(definition.getType().getGenericTypeParameters().get(1).getTypeParameter().getBoundaryModifier(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericTypeParameters().get(1).getTypeParameter().getBoundaryType(), isSimpleFqcnType("com.amazon.fenix.client.v2.ProductAdsProgram"));

        assertThat(definition.getType().getGenericTypeParameters().get(2).getTypeParameter().getName(), equalTo("RD"));
        assertThat(definition.getType().getGenericTypeParameters().get(2).getTypeParameter().getBoundaryModifier(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericTypeParameters().get(2).getTypeParameter().getBoundaryType(), isSimpleFqcnType("com.amazon.fenix.v2.ProductAdsRequestDecoration"));

        assertThat(definition.getType().getGenericTypeParameters().get(3).getTypeParameter().getName(), equalTo("A"));
        assertThat(definition.getType().getGenericTypeParameters().get(3).getTypeParameter().getBoundaryModifier(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericTypeParameters().get(3).getTypeParameter().getBoundaryType(), isSimpleFqcnType("com.amazon.fenix.model.ad.Ad"));

        assertThat(definition.getType().getGenericTypeParameters().get(4).getTypeParameter().getName(), equalTo("D"));
        assertThat(definition.getType().getGenericTypeParameters().get(4).getTypeParameter().getBoundaryModifier(), equalTo(EXTENDS));
        assertThat(definition.getType().getGenericTypeParameters().get(4).getTypeParameter().getBoundaryType(), isSimpleFqcnType("com.amazon.fenix.model.decorationentity.SponsoredOfferListingDecorationEntity"));
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
        assertThat(constructorArg.getType(), isSimpleFqcnType("com.amazon.Number"));
        assertThat(constructorArg.getType().getGenericTypeParameters(), hasSize(0));
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
        assertThat(constructorArg.getType().getGenericTypeParameters(), hasSize(1));

        final TypeDefinition genericArg = constructorArg.getType().getGenericTypeParameters().get(0);
        assertThat(genericArg, isSimpleFqcnType("com.amazon.Number"));
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

        assertThat(definition.getConstructors(), hasSize(1));
        final MethodDefinition constructor = definition.getConstructors().get(0);
        final Variable constructorArg = constructor.getArguments().get(0);
        final TypeParameter typeParameter = constructorArg.getType().getGenericTypeParameters().get(0).getTypeParameter();
        assertThat(typeParameter.getName(), equalTo("T"));
        assertThat(typeParameter.getBoundaryModifier(), equalTo(EXTENDS));
        assertThat(typeParameter.getBoundaryType(), isSimpleFqcnType("com.amazon.Number"));
    }

    @Test
    public void extractsExtendsParent() throws Exception {
        final ClassDefinition definition = parser.parse(from(
                "import com.amazon.Number;\n" +
                "import com.amazon.List;\n" +
                "\n" +
                "public class Testname extends List {\n" +
                "\n" +
                "\tpublic Testname() { }\n" +
                "\n" +
                "}"
        ));

        assertThat(definition.getParentTypes(), contains(isSimpleFqcnType("com.amazon.List")));
    }

    @Test
    public void extractsImplementsParents() throws Exception {
        final ClassDefinition definition = parser.parse(from(
                "import com.amazon.Number;\n" +
                "import com.amazon.List;\n" +
                "\n" +
                "public class Testname implements List, Number {\n" +
                "\n" +
                "\tpublic Testname() { }\n" +
                "\n" +
                "}"
        ));

        assertThat(definition.getParentTypes(), contains(isSimpleFqcnType("com.amazon.List"), isSimpleFqcnType("com.amazon.Number")));
    }

    @Test
    public void byDefaultClassesExtendJavaLangObject() throws Exception {
        final ClassDefinition definition = parser.parse(from(
                "public class Testname { }"
        ));

        assertThat(definition.getParentTypes(), contains(isSimpleFqcnType("java.lang.Object")));
    }

    @Test
    public void extractsFullyQualifiedTypesProperly() throws Exception {
        final ClassDefinition definition = parser.parse(from(
                "public class Testname implements com.amazon.List {\n" +
                "\n" +
                "\tpublic Testname(com.amazon.Number num) { }\n" +
                "\n" +
                "}"
        ));

        assertThat(definition.getParentTypes(), contains(isSimpleFqcnType("com.amazon.List")));
        assertThat(definition.getConstructors().get(0).getArguments().get(0).getType(), isSimpleFqcnType("com.amazon.Number"));
    }

    @Before
    public void setUp() throws Exception {
        /* some extra logging that is immensely useful when fixing unit tests in this class
         * unfortunately this is a hack that relies on logback being our logging framework
         * fortunately omitting this is safe and will not break unit tests
         */
//        setParserLoggingLevelTo(Level.TRACE);
    }

    @After
    public void tearDown() throws Exception {
//        setParserLoggingLevelTo(Level.DEBUG);
    }

    @SuppressWarnings("UnusedDeclaration")
    private static void setParserLoggingLevelTo(Level level) {
        ((Logger) LoggerFactory.getLogger("com.amazon.java.parser.antlr.StackTreeWalker")).setLevel(level);
    }

    private static Reader from(String s) {
        return new StringReader(s);
    }
}
