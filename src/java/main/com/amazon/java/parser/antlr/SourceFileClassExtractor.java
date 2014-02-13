package com.amazon.java.parser.antlr;

import com.amazon.java.MethodDefinition;
import com.amazon.java.TypeDefinition;
import com.amazon.java.TypeParameterContext;
import com.amazon.java.Variable;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceFileClassExtractor extends StackTreeListener {

    private static final Pattern PATTERN_EXTRACTSIMPLENAME = Pattern.compile(".*\\.([^.]+)");
    private State state = State.EXPECT_TYPE_DECLARATION;

    private final Map<String, String> imports = new HashMap<>();
    private final List<MethodDefinition> constructors = new ArrayList<>();
    private final TypeParameterContext classTypeParameterContext = new AntlrTypeParameterContext();
    private final List<TypeDefinition> parentTypes = new ArrayList<>();
    private String name;
    private String packageName;

    // temporary state when parsing method
    private List<Variable> methodParams;
    private String methodArgumentName;
    private TypeDefinition methodType;
    private TypeExtractor childTypeListener;

    // temp state for parsing generic parameters list in class definition
    private GenericParametersExtractor childGenericParametersExtractor;


    public SourceFileClassExtractor(StackTreeWalker walker) {
        super(walker);
    }

    @Override
    public void enterTypeDeclaration(@NotNull JavaParser.TypeDeclarationContext ctx) {
        if (state == State.EXPECT_TYPE_DECLARATION) {
            state = State.EXPECT_CLASS_DECLARATION;
        }
    }

    @Override
    public void enterType(@NotNull JavaParser.TypeContext ctx) {
        if(state == State.EXPECT_CONSTRUCTOR_ARGUMENT || state == State.INSIDE_CLASS_HEADER_PARENTS) {
            childTypeListener = new TypeExtractor(walker, this);
            walker.push(childTypeListener);
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
            state = State.INSIDE_CLASS_HEADER;
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
    public void enterFormalParameterList(@NotNull JavaParser.FormalParameterListContext ctx) {
        if (state == State.EXPECT_METHODS) {
            methodParams = new ArrayList<>();
            state = State.EXPECT_CONSTRUCTOR_ARGUMENT;
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
                    methodType
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
    public void visitTerminal(@NotNull TerminalNode node) {
        if (state == State.INSIDE_CLASS_HEADER) {
            if ("extends".equals(node.getText()) || "implements".equals(node.getText())) {
                state = State.INSIDE_CLASS_HEADER_PARENTS;
            }
        }
    }

    @Override
    public void enterTypeParameters(@NotNull JavaParser.TypeParametersContext ctx) {
        if (state == State.INSIDE_CLASS_HEADER) {
            childGenericParametersExtractor = new GenericParametersExtractor(walker, this);
            walker.push(childGenericParametersExtractor);
        }
    }

    @Override
    public void resume() {
        if (state == State.EXPECT_CONSTRUCTOR_ARGUMENT) {
            methodType = childTypeListener.makeType();
            childTypeListener = null;
        } else if (state == State.INSIDE_CLASS_HEADER) {
            // nothing to do, childGenericParametersExtractor should have filled our currentGenericContext
            childGenericParametersExtractor = null;
        } else if (state == State.INSIDE_CLASS_HEADER_PARENTS) {
            parentTypes.add(childTypeListener.makeType());
            childTypeListener = null;
        }
    }

    public List<MethodDefinition> getConstructors() {
        return constructors;
    }

    public List<TypeDefinition> getParentTypes() {
        return parentTypes;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Map<String, String> getImports() {
        return imports;
    }

    public TypeParameterContext getCurrentGenericContext() {
        return classTypeParameterContext;
    }

    public TypeParameterContext getClassTypeParameterContext() {
        return classTypeParameterContext;
    }

    static String extractClassName(String fqcn) {
        final Matcher matcher = PATTERN_EXTRACTSIMPLENAME.matcher(fqcn);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return fqcn;
    }

    private static enum State {
        EXPECT_TYPE_DECLARATION,
        EXPECT_CLASS_DECLARATION,
        INSIDE_CLASS_HEADER,
        READ_PACKAGE,
        EXPECT_METHODS,
        EXPECT_CONSTRUCTOR_ARGUMENT,
        INSIDE_CLASS_HEADER_PARENTS,
    }


}
