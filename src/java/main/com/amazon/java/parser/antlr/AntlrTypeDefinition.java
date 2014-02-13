package com.amazon.java.parser.antlr;

import java.util.List;

import com.amazon.java.TypeDefinition;
import com.amazon.java.TypeParameter;

public class AntlrTypeDefinition implements TypeDefinition {

    private final String fqcn;
    private final List<TypeDefinition> genericTypes;
    private final TypeParameter genericParam;

    public AntlrTypeDefinition(String fqcn, List<TypeDefinition> genericTypes, TypeParameter genericParam) {
        this.fqcn = fqcn;
        this.genericTypes = genericTypes;
        this.genericParam = genericParam;
    }

    @Override
    public String getFqcn() {
        return fqcn;
    }

    @Override
    public List<TypeDefinition> getGenericTypeParameters() {
        return genericTypes;
    }

    @Override
    public TypeParameter getTypeParameter() {
        return genericParam;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AntlrTypeDefinition that = (AntlrTypeDefinition) o;

        if (fqcn != null ? !fqcn.equals(that.fqcn) : that.fqcn != null) return false;
        if (genericParam != null ? !genericParam.equals(that.genericParam) : that.genericParam != null) return false;
        if (genericTypes != null ? !genericTypes.equals(that.genericTypes) : that.genericTypes != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fqcn != null ? fqcn.hashCode() : 0;
        result = 31 * result + (genericTypes != null ? genericTypes.hashCode() : 0);
        result = 31 * result + (genericParam != null ? genericParam.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (fqcn != null) {
            sb.append(fqcn);
        } else {
            sb.append(genericParam.toString());
        }
        if (genericTypes != null && !genericTypes.isEmpty()) {
            sb.append('<');
            for (TypeDefinition genericType : genericTypes) {
                sb.append(genericType).append(", ");
            }
            sb.append('>');
        }
        return sb.toString();
    }
}
