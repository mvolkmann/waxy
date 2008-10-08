package com.ociweb.xml;

import java.util.HashSet;
import java.util.Set;

/* package */ final class ElementMetadata {

    private final String qualifiedName;
    private final boolean isInComment;
    private final Set<String> prefixes = new HashSet<String>();

    /* package */ ElementMetadata(final String qualifiedName, final boolean isInComment) {
        this.qualifiedName = qualifiedName;
        this.isInComment = isInComment;
    }

    public String getName() {
        return qualifiedName;
    }

    public boolean isInComment() {
        return isInComment;
    }

    public void addPrefix(final String prefix) {
        prefixes.add(prefix);
    }

    public boolean containsPrefix(String prefix) {
        return prefixes.contains(prefix);
    }
}