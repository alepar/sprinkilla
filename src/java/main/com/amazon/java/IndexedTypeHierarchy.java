package com.amazon.java;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazon.java.parser.antlr.AntlrTypeDefinition;

public class IndexedTypeHierarchy implements TypeHierarchy {

    private final ClassDefinitionProvider provider;

    public IndexedTypeHierarchy(ClassDefinitionProvider provider) {
        this.provider = provider;
    }

    @Override
    public GuessedTypeParameters isAssignable(TypeDefinition src, TypeDefinition dest) {
        final GuessedTypeParameters guessedTypeParameters = new GuessedTypeParameters();
        if (isAssignable(src, dest, guessedTypeParameters, true)) {
            return guessedTypeParameters;
        } else {
            return null;
        }
    }

    private boolean isAssignable(TypeDefinition src, TypeDefinition dest, GuessedTypeParameters resolvedTypes, boolean allowSubclasses) {
        if (src.getFqcn() != null) {

            // src is not a captured type
            if(dest.getFqcn() != null) {
                // both src and dst is not a captured type
                if (src.getFqcn().equals(dest.getFqcn())) {
                    // types are equal, check for covariance
                    final List<TypeDefinition> srcTypeParams = src.getGenericTypeParameters();
                    final List<TypeDefinition> dstTypeParams = dest.getGenericTypeParameters();
                    if (srcTypeParams.size() != dstTypeParams.size()) {
                        return false;
                    }

                    for (int i=0; i< srcTypeParams.size(); i++) {
                        if(!isAssignable(srcTypeParams.get(i), dstTypeParams.get(i), resolvedTypes, false)) {
                            return false;
                        }
                    }

                    return true;
                } else if (allowSubclasses) {
                    // types are not equal, try to go up the inheritance chain
                    final Set<Deque<TypeDefinition>> paths = listAllInheritancePaths(src);
                    for (Deque<TypeDefinition> path : paths) {
                        for (TypeDefinition parent : path) {
                            if (dest.getFqcn().equals(parent.getFqcn())) { // if we found parent with needed type
                                return isAssignable(parent, dest, resolvedTypes, false);         // just check for covariance
                            }
                        }
                    }
                    return false;
                } else {
                    return false;
                }
            } else {
                // dest is captured type, we're inside covariance check
                //noinspection SimplifiableIfStatement
                if (dest.getTypeParameter().getBoundaryModifier().equals(TypeParameter.BoundaryModifier.EXTENDS)) {
                    final boolean assignable = isAssignable(src, dest.getTypeParameter().getBoundaryType(), resolvedTypes, true);
                    if (assignable) {
                        resolvedTypes.addResolvedType(dest.getTypeParameter(), src);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    // SUPER not supported
                    return false;
                }

            }
        } else {
            //src is captured type
            final TypeDefinition newSrc = src.getTypeParameter().getBoundaryType();
            if (src.getTypeParameter().getBoundaryModifier() != TypeParameter.BoundaryModifier.EXTENDS) {
                // SUPER not supported
                return false;
            }
            TypeDefinition newDest;
            if (dest.getFqcn() != null) {
                newDest = dest;
            } else if (dest.getTypeParameter().getBoundaryModifier() != TypeParameter.BoundaryModifier.EXTENDS) {
                // SUPER not supported
                return false;
            } else {
                newDest = dest.getTypeParameter().getBoundaryType();
            }

            if (isAssignable(newSrc, newDest, resolvedTypes, true)) {
                if (src.getTypeParameter() != null) {
                    resolvedTypes.addResolvedType(src.getTypeParameter(), newSrc);
                }
                if (dest.getTypeParameter() != null) {
                    resolvedTypes.addResolvedType(dest.getTypeParameter(), newSrc);
                }
                return true;
            } else if(isAssignable(newDest, newSrc, resolvedTypes, true)) {
                if (src.getTypeParameter() != null) {
                    resolvedTypes.addResolvedType(src.getTypeParameter(), newDest);
                }
                if (dest.getTypeParameter() != null) {
                    resolvedTypes.addResolvedType(dest.getTypeParameter(), newDest);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private Set<Deque<TypeDefinition>> listAllInheritancePaths(TypeDefinition type) {
        final Deque<TypeDefinition> startPath = new ArrayDeque<>();
        startPath.add(type);
        return expandPath(startPath);
    }

    private Set<Deque<TypeDefinition>> expandPath(Deque<TypeDefinition> path) {
        final Set<Deque<TypeDefinition>> expanded = new HashSet<>();
        final TypeDefinition top = path.getLast();
        if (top.equals(TypeDefinition.JAVA_LANG_OBJECT)) {
            expanded.add(path);
        } else {
            final ClassDefinition topClassDef = provider.getFor(top.getFqcn());
            final List<TypeDefinition> parentsForTop = topClassDef.getParentTypes();
            if (parentsForTop == null || parentsForTop.isEmpty()) {
                throw new RuntimeException("no hierarchy data for class " + top.getFqcn());
            }
            for (TypeDefinition parent : parentsForTop) {
                final Deque<TypeDefinition>  expandedPath = new ArrayDeque<>(path);
                expandedPath.addLast(passAlongGenericTypeParams(top, topClassDef, parent));
                expanded.addAll(expandPath(expandedPath));
            }
        }
        return expanded;
    }

    private TypeDefinition passAlongGenericTypeParams(TypeDefinition child, ClassDefinition childClassDef, TypeDefinition parent) {
        if (parent.getGenericTypeParameters() == null || parent.getGenericTypeParameters().isEmpty()) {
            return parent;
        }

        final List<TypeDefinition> newParams = new ArrayList<>(parent.getGenericTypeParameters().size());

        for (TypeDefinition typeParam : parent.getGenericTypeParameters()) {
            if (typeParam.getTypeParameter() == null) {
                newParams.add(typeParam); // simple fqcn type param, no captured types, just pass along
            } else {
                final int matchingChildParamIndex = findTypeParamIndexByName(childClassDef.getType().getGenericTypeParameters(), typeParam.getTypeParameter().getName());
                newParams.add(child.getGenericTypeParameters().get(matchingChildParamIndex));
            }
        }

        return new AntlrTypeDefinition(
            parent.getFqcn(),
            newParams,
            parent.getTypeParameter()
        );
    }

    private static int findTypeParamIndexByName(List<TypeDefinition> typeParams, String paramName) {
        for (int i=0; i< typeParams.size(); i++) {
            if (typeParams.get(i).getTypeParameter().getName().equals(paramName)) {
                return i;
            }
        }

        throw new RuntimeException("assert failed - could not find matching type param");
    }

    private static class GuessedTypeParameters implements TypeHierarchy.GuessedTypeParameters {

        private final Map<TypeParameter, TypeDefinition> resolved = new HashMap<>();

        public void addResolvedType(TypeParameter typeParameter, TypeDefinition src) {
            resolved.put(typeParameter, src);
        }

        @Override
        public TypeDefinition typeFor(TypeParameter param) {
            return resolved.get(param);
        }
    }
}
