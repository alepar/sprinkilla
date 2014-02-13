package com.amazon.java.parser.antlr;

import com.amazon.java.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AntlrClassDefinition implements ClassDefinition {

    private final TypeDefinition type;
    private final List<MethodDefinition> constructors;
    private final List<TypeDefinition> parentTypes;

    public AntlrClassDefinition(SourceFileClassExtractor extractor) {
        this.parentTypes = extractor.getParentTypes().isEmpty() ? Collections.<TypeDefinition>singletonList(TypeDefinition.JAVA_LANG_OBJECT) : extractor.getParentTypes();
        this.constructors = extractor.getConstructors();
        this.type = new AntlrTypeDefinition(
                extractor.getPackageName() == null || extractor.getPackageName().isEmpty() ? extractor.getName() : extractor.getPackageName() + '.' + extractor.getName(),
                extractGenericTypes(extractor.getClassTypeParameterContext()),
                null
        );
    }

    private List<TypeDefinition> extractGenericTypes(TypeParameterContext ctx) {
        final List<TypeDefinition> types = new ArrayList<>(ctx.getParams().size());
        for (TypeParameter param : ctx.getParams()) {
            types.add(new AntlrTypeDefinition(
                    null,
                    Collections.<TypeDefinition>emptyList(),
                    param));
        }
        return types;
    }

    @Override
    public TypeDefinition getType() {
        return type;
    }

    @Override
    public List<MethodDefinition> getConstructors() {
        return constructors;
    }

    @Override
    public List<TypeDefinition> getParentTypes() {
        return parentTypes;
    }
}
