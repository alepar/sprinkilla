package com.amazon.java.parser.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.amazon.java.TypeDefinition;
import com.amazon.java.TypeParameter;

public class TypeExtractor extends StackTreeListener {

    private final SourceFileClassExtractor parent;
    private State state = State.FQCN_COMING;

    private List<TypeDefinition> genericTypes = new ArrayList<>();
    private String fqcn;
    private String generic;

    private TypeExtractor child;
    private StringBuilder terminals = new StringBuilder();

    public TypeExtractor(StackTreeWalker walker, SourceFileClassExtractor parent) {
        super(walker);
        this.parent = parent;
    }

    @Override
    public void visitTerminal(@NotNull TerminalNode node) {
        if (state == State.FQCN_COMING) {
            terminals.append(node.getText());
        }
    }

    @Override
    public void enterTypeArguments(@NotNull JavaParser.TypeArgumentsContext ctx) {
        state = State.FQCN_ENDED;
    }

    @Override
    public void exitClassOrInterfaceType(@NotNull JavaParser.ClassOrInterfaceTypeContext ctx) {
        final String accumulatedName = terminals.toString();
        final TypeParameter typeParameter = parent.getCurrentGenericContext().get(accumulatedName);
        if (typeParameter != null) {
            generic = accumulatedName;
        } else {
            fqcn = parent.getImports().get(accumulatedName) == null ? accumulatedName : parent.getImports().get(accumulatedName);
        }
    }

    @Override
    public void enterType(@NotNull JavaParser.TypeContext ctx) {
        child = new TypeExtractor(walker, parent);
        walker.push(child);
    }

    @Override
    public void exitType(@NotNull JavaParser.TypeContext ctx) {
        walker.pop();
    }

    @Override
    public void resume() {
        genericTypes.add(child.makeType());
    }

    public TypeDefinition makeType() {
        return new AntlrTypeDefinition(
                fqcn, genericTypes, generic == null ? null : parent.getClassTypeParameterContext().get(generic)
        );
    }

    private static enum State {
        FQCN_COMING, FQCN_ENDED
    }
}
