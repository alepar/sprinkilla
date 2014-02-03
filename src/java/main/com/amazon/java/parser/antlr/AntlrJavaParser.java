package com.amazon.java.parser.antlr;

import com.amazon.java.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.amazon.java.GenericParameter.BoundaryType;

public class AntlrJavaParser implements com.amazon.java.parser.JavaParser {

    private static final Pattern PATTERN_EXTRACTSIMPLENAME = Pattern.compile(".*\\.([^.]+)");

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
        EXPECT_BOUNDARY_NAME,
        READ_PACKAGE,
        EXPECT_METHODS,
        EXPECT_CONSTRUCTOR_PARAMETERS,
        EXPECT_CONSTRUCTOR_ARGUMENT, EXPECT_CONSTRUCTOR_ARGUMENT_TYPE_PARAMETERS,
    }

    public static class Listener extends JavaBaseListener implements ClassDefinition {

        private State state = State.EXPECT_TYPE_DECLARATION;

        private final Map<String, String> imports = new HashMap<>();

        private final List<AntlrGenericParameter> classGenericParameters = new ArrayList<>();
        private final List<MethodDefinition> constructors = new ArrayList<>();
        private String name;
        private String packageName;

        private List<Variable> methodParams;
        private String methodArgumentFqcn;
        private String methodArgumentName;
        private List<AntlrGenericParameter> methodArgumentGenericParameters;

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
                state = State.EXPECT_BOUNDARY_NAME;
            }
        }

        @Override
        public void enterTypeParameter(@NotNull JavaParser.TypeParameterContext ctx) {
            if (state == State.EXPECT_BOUNDARY_NAME) {
                final AntlrGenericParameter parameter = extractGenericParameter(ctx);
                classGenericParameters.add(parameter);
            }
        }

        @Override
        public void exitTypeParameters(@NotNull JavaParser.TypeParametersContext ctx) {
            if (state == State.EXPECT_BOUNDARY_NAME) {
                state = State.EXPECT_METHODS;
            }
        }

        @Override
        public void enterTypeArguments(@NotNull JavaParser.TypeArgumentsContext ctx) {
            if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT) {
                state = State.EXPECT_CONSTRUCTOR_ARGUMENT_TYPE_PARAMETERS;
            }
        }

        @Override
        public void exitTypeArguments(@NotNull JavaParser.TypeArgumentsContext ctx) {
            if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT_TYPE_PARAMETERS) {
                state = State.EXPECT_CONSTRUCTOR_ARGUMENT;
            }
        }

        @Override
        public void enterTypeArgument(@NotNull JavaParser.TypeArgumentContext ctx) {
            if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT_TYPE_PARAMETERS) {
                final AntlrGenericParameter parameter = extractGenericParameter(ctx);
                methodArgumentGenericParameters.add(parameter);
            }
        }

        @Override
        public void exitTypeArgument(@NotNull JavaParser.TypeArgumentContext ctx) {
            if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT_TYPE_PARAMETERS) {
                final AntlrGenericParameter param = methodArgumentGenericParameters.get(methodArgumentGenericParameters.size() - 1);
                if (param.boundaryType == BoundaryType.NO_WILDCARD) {
                    final AntlrGenericParameter classDefinitionParameter = findParameter(param.getName());
                    if (classDefinitionParameter != null) {
                        methodArgumentGenericParameters.remove(methodArgumentGenericParameters.size()-1);
                        methodArgumentGenericParameters.add(classDefinitionParameter);
                    }
                }
            }
        }

        @Override
        public void enterClassOrInterfaceType(@NotNull JavaParser.ClassOrInterfaceTypeContext ctx) {
            if (state == State.EXPECT_BOUNDARY_NAME || state == State.EXPECT_CONSTRUCTOR_ARGUMENT || state == State.EXPECT_CONSTRUCTOR_ARGUMENT_TYPE_PARAMETERS) {
                final String classname = ctx.getChild(0).getText();
                final String fqcn = imports.get(classname);

                if (state == State.EXPECT_BOUNDARY_NAME) {
                    classGenericParameters.get(classGenericParameters.size()-1).boundaryFqcn = fqcn == null ? classname : fqcn;
                } else if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT) {
                    methodArgumentFqcn = fqcn;
                } else if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT_TYPE_PARAMETERS) {
                    methodArgumentGenericParameters.get(methodArgumentGenericParameters.size()-1).boundaryFqcn = fqcn == null ? classname : fqcn;
                }
            }
        }

        @Override
        public void enterClassBody(@NotNull JavaParser.ClassBodyContext ctx) {
            state = State.EXPECT_METHODS;
        }

        @Override
        public void enterPackageDeclaration(@NotNull JavaParser.PackageDeclarationContext ctx) {
            state = State.READ_PACKAGE;
        }

        @Override
        public void exitPackageDeclaration(@NotNull JavaParser.PackageDeclarationContext ctx) {
            state = State.EXPECT_TYPE_DECLARATION;
        }

        @Override
        public void enterQualifiedName(@NotNull JavaParser.QualifiedNameContext ctx) {
            if (state == State.READ_PACKAGE) {
                packageName = ctx.getText();
            }
        }

        @Override
        public void enterConstructorDeclaration(@NotNull JavaParser.ConstructorDeclarationContext ctx) {
            if (state == State.EXPECT_METHODS) {
                state = State.EXPECT_CONSTRUCTOR_PARAMETERS;
            }
        }

        @Override
        public void enterFormalParameterList(@NotNull JavaParser.FormalParameterListContext ctx) {
            if (state == State.EXPECT_CONSTRUCTOR_PARAMETERS) {
                methodParams = new ArrayList<>();
                state = State.EXPECT_CONSTRUCTOR_ARGUMENT;
            }
        }

        @Override
        public void enterFormalParameter(@NotNull JavaParser.FormalParameterContext ctx) {
            if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT) {
                methodArgumentGenericParameters = new ArrayList<>();
            }
        }

        @Override
        public void enterVariableDeclaratorId(@NotNull JavaParser.VariableDeclaratorIdContext ctx) {
            if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT) {
                methodArgumentName = ctx.getText();
            }
        }

        @Override
        public void exitFormalParameter(@NotNull JavaParser.FormalParameterContext ctx) {
            if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT) {
                methodParams.add(new AntlrVariable(
                        methodArgumentName,
                        new AntlrTypeDefinition(methodArgumentFqcn, convertParams(methodArgumentGenericParameters))
                ));
            }
        }

        @Override
        public void exitConstructorDeclaration(@NotNull JavaParser.ConstructorDeclarationContext ctx) {
            if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT) {
                constructors.add(new AntlrMethodDefinition(methodParams, name));
                methodParams = null;
            }
        }

        @Override
        public TypeDefinition getType() {
            return new AntlrTypeDefinition(
                    packageName == null || packageName.isEmpty() ? name : packageName + '.' + name,
                    convertParams(classGenericParameters)
             );
        }

        @Override
        public List<MethodDefinition> getConstructors() {
            return constructors;
        }

        @Override
        public void enterEveryRule(@NotNull ParserRuleContext ctx) {
//            System.out.println("enter " + ctx.getClass().getSimpleName());
        }

        @Override
        public void exitEveryRule(@NotNull ParserRuleContext ctx) {
//            System.out.println("exit " + ctx.getClass().getSimpleName());
        }

        private AntlrGenericParameter findParameter(String name) {
            for (AntlrGenericParameter parameter : classGenericParameters) {
                if (parameter.getName().equals(name)) {
                    return parameter;
                }
            }

            return null;
        }
    }

    private static AntlrGenericParameter extractGenericParameter(ParserRuleContext ctx) {
        return new AntlrGenericParameter(
                            ctx.getChild(0).getText(),
                            ctx.getChildCount() > 1 ? BoundaryType.EXTENDS : BoundaryType.NO_WILDCARD
                    );
    }

    private static String extractClassName(String fqcn) {
        final Matcher matcher = PATTERN_EXTRACTSIMPLENAME.matcher(fqcn);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return fqcn;
    }

    private static class AntlrGenericParameter implements GenericParameter {

        private final String name;
        private final BoundaryType boundaryType;
        private String boundaryFqcn;

        private AntlrGenericParameter(String name, BoundaryType boundaryType) {
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
        public String getBoundaryFqcn() {
            return boundaryFqcn;
        }
    }

    private static class AntlrVariable implements Variable {
        private final String name;
        private final TypeDefinition type;

        private AntlrVariable(String name, TypeDefinition type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public TypeDefinition getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static class AntlrTypeDefinition implements TypeDefinition {
        private final String fqcn;
        private final List<GenericParameter> args;

        public AntlrTypeDefinition(String fqcn, List<GenericParameter> args) {
            this.fqcn = fqcn;
            this.args = args;
        }

        @Override
        public String getFqcn() {
            return fqcn;
        }

        @Override
        public List<GenericParameter> getGenericParameters() {
            return args;
        }
    }

    private static class AntlrMethodDefinition implements MethodDefinition {
        private final List<Variable> vars;
        private final String name;

        private AntlrMethodDefinition(List<Variable> vars, String name) {
            this.vars = vars;
            this.name = name;
        }

        @Override
        public List<Variable> getArguments() {
            return vars;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<GenericParameter> convertParams(List<AntlrGenericParameter> args) {
        return Collections.unmodifiableList((List) args);  // umodifiable to ensure type safety
    }

}
