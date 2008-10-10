package com.ociweb.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * Each instance of this class represents the information about one XML
 * <code>Element</code>, its contents, and its context.
 */
/* package */ final class ElementMetadata {

    /**
     * XML <a href="http://www.w3.org/TR/2008/PER-xml-20080205/#NT-Name">
     * <code>Name</code></a> of an XML <code>Element</code>, with optional XML
     * Namespace "colon" prefix.
     */
    private final String qualifiedName;

    /**
     * <code>true</code> if and only if this XML Element is the start/root/base
     * of an XML Fragment that is a comment representing possible XML elements.
     */
    private final boolean isInComment;

    /**
     * XML Namespace prefixes defined in this XML Element.
     */
    private final Set<String> prefixes = new HashSet<String>();

    /**
     * Set of all attribute names defined in this XML Element.
     */
    private final Set<String> definedAttributeNames = new HashSet<String>();

    /* package */ ElementMetadata(final String qualifiedName, final boolean isInComment) {
        this.qualifiedName = qualifiedName;
        this.isInComment = isInComment;
    }

    public void addPrefix(final String prefix) {
        prefixes.add(prefix);
    }

    public boolean containsPrefix(final String prefix) {
        return prefixes.contains(prefix);
    }

    public void defineAttribute(final String qName) {
        if (definedAttributeNames.contains(qName)) {
            throw new IllegalArgumentException("The attribute \"" + qName
                    + "\" is defined twice in this element.");
        } else {
            definedAttributeNames.add(qName);
        }
    }

    public String getName() {
        return qualifiedName;
    }

    public boolean isInComment() {
        return isInComment;
    }
}