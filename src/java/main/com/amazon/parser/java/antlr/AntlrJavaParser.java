package com.amazon.parser.java.antlr;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.amazon.parser.java.ClassDefinition;

import static com.amazon.parser.java.GenericArgument.BoundaryType;

public class AntlrJavaParser implements com.amazon.parser.java.JavaParser {

    @Override
    public ClassDefinition parse(Reader r) {
        try {
            final JavaLexer lexer = new JavaLexer(new ANTLRInputStream(r));
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final JavaParser parser = new JavaParser(tokens);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            final JavaParser.CompilationUnitContext tree = parser.compilationUnit();

            final ParseTreeWalker walker = new ParseTreeWalker(); // create standard walker
            final Listener extractor = new Listener();
            walker.walk(extractor, tree); // initiate walk of tree with listener
            return extractor;
        } catch (IOException e) {
            throw new RuntimeException("could not read java source", e);
        }
    }

    private static enum State {
        EXPECT_TYPE_DECLARATION,
        EXPECT_CLASS_DECLARATION,
        EXPECT_TYPE_PARAMETER,
        EXPECT_BOUNDARY_NAME,
        END,
    }

    public static class Listener extends JavaBaseListener implements ClassDefinition {

        private State state = State.EXPECT_TYPE_DECLARATION;

        private final Map<String, String> imports = new HashMap<>();

        private final List<AntlrJavaParser.GenericArgument> genericArguments = new ArrayList<>();
        private String name;

        @Override
        public void enterTypeDeclaration(@NotNull JavaParser.TypeDeclarationContext ctx) {
            if (state == State.EXPECT_TYPE_DECLARATION) {
                state = State.EXPECT_CLASS_DECLARATION;
            }
        }

        @Override
        public void enterImportDeclaration(@NotNull JavaParser.ImportDeclarationContext ctx) {
            final String fqcn = ctx.getChild(1).getText();
            imports.put(extractClassName(fqcn), fqcn);
        }

        @Override
        public void enterClassDeclaration(@NotNull JavaParser.ClassDeclarationContext ctx) {
            if (state == State.EXPECT_CLASS_DECLARATION) {
                name = ctx.getChild(1).getText();
                state = State.EXPECT_TYPE_PARAMETER;
            }
        }

        @Override
        public void enterTypeParameter(@NotNull JavaParser.TypeParameterContext ctx) {
            if (state == State.EXPECT_TYPE_PARAMETER) {
                genericArguments.add(new AntlrJavaParser.GenericArgument(
                        ctx.getChild(0).getText(),
                        ctx.getChildCount() > 1 ? BoundaryType.EXTENDS : BoundaryType.NONE
                ));
                if (ctx.getChildCount() > 1) {
                    state = State.EXPECT_BOUNDARY_NAME;
                }
            }
        }

        @Override
        public void enterClassOrInterfaceType(@NotNull JavaParser.ClassOrInterfaceTypeContext ctx) {
            if (state == State.EXPECT_BOUNDARY_NAME) {
                final String classname = ctx.getChild(0).getText();
                final String fqcn = imports.get(classname);
                genericArguments.get(genericArguments.size()-1).boundaryName = fqcn == null ? classname : fqcn;
                state = State.EXPECT_TYPE_PARAMETER;
            }
        }

        @Override
        public void enterClassBody(@NotNull JavaParser.ClassBodyContext ctx) {
            state = State.END; //declaration finished, nothing more to expect
        }

        @Override
        public String getName() {
            return name;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<com.amazon.parser.java.GenericArgument> getGenericArguments() {
            return Collections.unmodifiableList((List) genericArguments); // umodifiable to ensure type safety
        }

    }

    private static String extractClassName(String fqcn) {
        return fqcn.replaceFirst(".*\\.([^.]+)", "$1");
    }

    private static class GenericArgument implements com.amazon.parser.java.GenericArgument {

        private final String name;
        private final BoundaryType boundaryType;
        private String boundaryName;

        private GenericArgument(String name, BoundaryType boundaryType) {
            this.name = name;
            this.boundaryType = boundaryType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public BoundaryType getBoundaryType() {
            return boundaryType;
        }

        @Override
        public String getBoundaryName() {
            return boundaryName;
        }
    }

}
