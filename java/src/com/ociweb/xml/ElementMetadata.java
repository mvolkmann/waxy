package com.ociweb.xml;

import java.util.*;

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
    private final String elementQualifiedName;

    /**
     * <code>true</code> if and only if this XML Element is the start/root/base
     * of an XML Fragment that is a comment representing possible XML elements.
     */
    private final boolean isCommentElement;

    /**
     * <code>ElementMetadata</code> for the XML Element that contains this
     * Element in the XML output stream.
     */
    private final ElementMetadata parent;

    /**
     * XML Namespace prefixes defined in this XML Element.
     */
    private final Map<String, String> namespacePrefixToURLMap
            = new HashMap<String, String>();

    /**
     * XML <a href="http://www.w3.org/TR/REC-xml-names/#NT-Prefix">prefixes</a>
     * used in the current XML <code>Element</code>.
     */
    private final Set<String> namespacePrefixesUsed = new HashSet<String>();

    /**
     * Set of all attribute names defined in this XML Element. Contains all
     * "qualified" (IE: namespace prefixed) attribute names.
     */
    private final Set<String> definedAttributeNames = new HashSet<String>();

    /**
     * Set of all "expanded" attribute names. (IE: Namespace URL + ':' +
     * Attribute Name) Used to enforce uniqueness.
     */
    private final Set<String> expendedAttributeNames = new HashSet<String>();

    /* package */ ElementMetadata(
            final String prefix,
            final String name,
            final boolean isCommentElement,
            final ElementMetadata parent,
            final boolean checkMe)
    {
        final String qualifiedName = buildQualifiedName(prefix, name, checkMe);

        this.elementQualifiedName = qualifiedName;
        this.isCommentElement = isCommentElement;
        this.parent = parent;
    }

    /**
     * Build <a href="http://www.w3.org/TR/REC-xml-names/">XML Namespace</a>
     * <a href="http://www.w3.org/TR/REC-xml-names/#NT-QName">"Qualified Name"</a>
     * for an XML element or attribute.
     * 
     * @param prefix the namespace prefix for the attribute
     * @param name the element or attribute name
     * @param checkMe 
     * @return <a href="http://www.w3.org/TR/REC-xml-names/">XML Namespace</a>
     *     <a href="http://www.w3.org/TR/REC-xml-names/#NT-QName">"Qualified Name"</a>
     */
    public String buildQualifiedName(
            final String prefix,
            final String name,
            final boolean checkMe)
    {
        final boolean hasPrefix = (prefix != null) && (prefix.length() > 0);

        if (checkMe) {
            if (hasPrefix) {
                XMLUtil.verifyName(prefix);
                namespacePrefixesUsed.add(prefix);
            }
            XMLUtil.verifyName(name);
        }

        return (hasPrefix) ? (prefix + ':' + name) : name;
    }

    public boolean containsNamespacePrefix(final String prefix) {
        return namespacePrefixToURLMap.containsKey(prefix);
    }

    public String defineAttribute(
            final String prefix,
            final String name,
            final boolean checkMe)
    {
        final String qualifiedAttributeName = buildQualifiedName(prefix, name,
                checkMe);
        if (definedAttributeNames.contains(qualifiedAttributeName)) {
            throw new IllegalArgumentException("The attribute \""
                    + qualifiedAttributeName
                    + "\" is defined twice in this element.");
        }

        final String namespaceURL = getNamespaceUrl(prefix);
        if (namespaceURL != null) {
            final String expandedAttributeName = namespaceURL + name;
            if (expendedAttributeNames.contains(expandedAttributeName)) {
                throw new IllegalArgumentException(
                        "The attribute \"xmlns:ns=\"" + namespaceURL + "\" ns:"
                                + name + "\" is defined twice in this element.");
            }
            expendedAttributeNames.add(expandedAttributeName);
        }

        definedAttributeNames.add(qualifiedAttributeName);
        return qualifiedAttributeName;
    }

    public void defineNamespace(final String prefix, final String uri) {
        namespacePrefixToURLMap.put(prefix, uri);
    }

    private String getNamespaceUrl(final String prefix) {
        final String namespaceURL = namespacePrefixToURLMap.get(prefix);
        if (namespaceURL == null && parent != null)
            return parent.getNamespaceUrl(prefix);
        return namespaceURL;
    }

    public String getQualifiedName() {
        return elementQualifiedName;
    }

    public boolean isCommentElement() {
        return isCommentElement;
    }

    /**
     * @return <code>true</code> if the given XML Namespace <code>prefix</code>
     *         is defined in this XML Element, or any of its parent Elements.
     */
    public boolean isNamespacePrefixInScope(final String prefix) {
        if (this.containsNamespacePrefix(prefix))
            return true;
        else if (parent != null)
            return parent.isNamespacePrefixInScope(prefix);
        else
            return false;
    }

    /**
     * Verifies that all the pending namespace prefix are currently in scope.
     * @throws IllegalArgumentException if any aren't in scope
     */
    public void verifyOutstandingNamespacePrefixes() {
        for (final String prefix : namespacePrefixesUsed) {
            if (!isNamespacePrefixInScope(prefix)) {
                throw new IllegalArgumentException(
                    "The namespace prefix \"" + prefix + "\" isn't in scope.");
            }
        }

        namespacePrefixesUsed.clear();
    }
}