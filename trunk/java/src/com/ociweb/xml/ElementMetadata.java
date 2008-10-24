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
     * <i>(...except for the default namespace.)</i>
     */
    private final Map<String, String> namespacePrefixToURLMap
            = new HashMap<String, String>();

    /**
     * <code>true</code> when the default namespace has been defined in this
     * XML <code>Element</code>.
     */
    private boolean defaultNamespaceDefined = false;
    
    /**
     * Set of all attribute names defined in this XML Element. Contains all
     * "qualified" (IE: namespace prefixed) attribute names.
     */
    private final Set<String> definedAttributeNames = new HashSet<String>();

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
        final boolean hasPrefix = XMLUtil.hasValue(prefix);

        if (checkMe) {
            if (hasPrefix)
                XMLUtil.verifyName(prefix);
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

        definedAttributeNames.add(qualifiedAttributeName);
        return qualifiedAttributeName;
    }

    public void defineNamespace(final String prefix, final String uri) {
        final boolean hasPrefix = XMLUtil.hasValue(prefix);
        if (hasPrefix) {
            namespacePrefixToURLMap.put(prefix, uri);
        } else {
            defaultNamespaceDefined = true;
        }
    }

    /**
     * Get the URL string value for the given namespace prefix in the current
     * scope.  Returns <code>null</code> if undefined.
     *
     * @param prefix
     * @return The URL for the given namespace <code>prefix</code>;
     *         <code>null</code> if this <code>prefix</code> is not defined
     *         anywhere on the Element stack; empty string (<code>""</code>) if
     *         the namespace is explicitly <b>undefined</b> in the current
     *         scope.
     */
    private String getNamespaceUrl(final String prefix) {
        final String namespaceURL = namespacePrefixToURLMap.get(prefix);
        if (namespaceURL == null && parent != null)
            return parent.getNamespaceUrl(prefix);
        return namespaceURL;
    }

    public String getQualifiedName() {
        return elementQualifiedName;
    }

    /**
     * @param prefix
     * @return The URL for the given namespace <code>prefix</code>.
     * @throws IllegalArgumentException if the namespace <code>prefix</code> is
     *          not defined in the current scope.
     */
    private String getRequiredNamespaceURL(final String prefix) {
        final String namespaceURL = getNamespaceUrl(prefix);
        if (namespaceURL == null || "".equals(namespaceURL)) {
            throw new IllegalArgumentException(
                    "The namespace prefix \"" + prefix + "\" isn't in scope.");
        }
        return namespaceURL;
    }

    public boolean isCommentElement() {
        return isCommentElement;
    }

    private void verifyAttributeNamesWithinStartTag() {
        final Set<String> expandedAttributeNames = new HashSet<String>();

        for (final String qualifiedAttributeName : definedAttributeNames) {
            final int colonIndex = qualifiedAttributeName.indexOf(':');
            if (colonIndex > 0) {
                final String prefix = qualifiedAttributeName.substring(0, colonIndex);
                final String name = qualifiedAttributeName.substring(colonIndex + 1);
                final String namespaceURL = getRequiredNamespaceURL(prefix);

                final String expandedName = namespaceURL + ':' + name;
                if (expandedAttributeNames.contains(expandedName)) {
                    throw new IllegalArgumentException(
                            "The attribute <xmlns:ns=\"" + namespaceURL
                                    + "\" ns:" + name
                                    + "> is defined twice in this element.");
                } else {
                    expandedAttributeNames.add(expandedName);
                }
            }
        }
    }

    private void verifyElementNamespaceUsage() {
        final int colonIndex = elementQualifiedName.indexOf(':');
        if (colonIndex > 0) {
            final String prefix = elementQualifiedName.substring(0, colonIndex);
            getRequiredNamespaceURL(prefix);
        }
    }

    public void verifyNamespaceData(
            final String prefix,
            final String uri,
            final String schemaPath)
    {
        final boolean hasPrefix = XMLUtil.hasValue(prefix);
        if (hasPrefix) XMLUtil.verifyName(prefix);
        XMLUtil.verifyURI(uri);
        if (schemaPath != null) XMLUtil.verifyURI(schemaPath);
    
        // Verify that the prefix isn't already defined
        // on the current element.
        if (hasPrefix) {
            if (containsNamespacePrefix(prefix)) {
                throw new IllegalArgumentException(
                    "The namespace prefix \"" + prefix +
                    "\" is already defined on the current element.");
            }
        } else if (defaultNamespaceDefined) {
            throw new IllegalArgumentException("The default namespace " +
                "is already defined on the current element.");
        }
    }

    /**
     * Verifies that all the pending namespace prefix are currently in scope.
     * @throws IllegalArgumentException if any aren't in scope
     */
    public void verifyOutstandingNamespacePrefixes() {
        verifyElementNamespaceUsage();
        verifyAttributeNamesWithinStartTag();
    }
}