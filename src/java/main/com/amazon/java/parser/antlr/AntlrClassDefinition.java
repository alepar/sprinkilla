package com.amazon.java.parser.antlr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.amazon.java.ClassDefinition;
import com.amazon.java.GenericContext;
import com.amazon.java.GenericParameter;
import com.amazon.java.MethodDefinition;
import com.amazon.java.TypeDefinition;

class AntlrClassDefinition implements ClassDefinition {

    public static final AntlrTypeDefinition JAVA_LANG_OBJECT = new AntlrTypeDefinition("java.lang.Object", Collections.<TypeDefinition>emptyList(), null);

    private final TypeDefinition type;
    private final List<MethodDefinition> constructors;
    private final List<TypeDefinition> parentTypes;

    public AntlrClassDefinition(SourceFileClassExtractor extractor) {
        this.parentTypes = extractor.getParentTypes().isEmpty() ? Collections.<TypeDefinition>singletonList(JAVA_LANG_OBJECT) : extractor.getParentTypes();
        this.constructors = extractor.getConstructors();
        this.type = new AntlrTypeDefinition(
                extractor.getPackageName() == null || extractor.getPackageName().isEmpty() ? extractor.getName() : extractor.getPackageName() + '.' + extractor.getName(),
                extractGenericTypes(extractor.getClassGenericContext()),
                null
        );
    }

    private List<TypeDefinition> extractGenericTypes(GenericContext ctx) {
        final List<TypeDefinition> types = new ArrayList<>(ctx.getParams().size());
        for (GenericParameter param : ctx.getParams()) {
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
