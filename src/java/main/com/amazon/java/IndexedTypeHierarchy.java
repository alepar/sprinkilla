package com.amazon.java;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IndexedTypeHierarchy implements MutableTypeHierarchy {

    private final Map<String, Set<TypeDefinition>> allParents = new HashMap<>();

    @Override
    public void addDefinition(ClassDefinition definition) {
        getParentsFor(definition.getType().getFqcn()).addAll(definition.getParentTypes());
    }

    @Override
    public boolean isAssignable(TypeDefinition src, TypeDefinition dest) {
        final Set<Deque<TypeDefinition>> paths = listAllInheritancePaths(src);
        for (Deque<TypeDefinition> path : paths) {
            if (path.contains(dest)) {
                return true;
            }
        }
        return false;
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
            final Set<TypeDefinition> parentsForTop = allParents.get(top.getFqcn());
            if (parentsForTop == null || parentsForTop.isEmpty()) {
                throw new RuntimeException("no hierarchy data for class " + top.getFqcn());
            }
            for (TypeDefinition parent : parentsForTop) {
                final Deque<TypeDefinition>  expandedPath = new ArrayDeque<>(path);
                expandedPath.addLast(parent);
                expanded.addAll(expandPath(expandedPath));
            }
        }
        return expanded;
    }

    private Set<TypeDefinition> getParentsFor(String fqcn) {
        Set<TypeDefinition> set = allParents.get(fqcn);
        if (set == null) {
            set = new HashSet<>();
            allParents.put(fqcn, set);
        }
        return set;
    }

}
