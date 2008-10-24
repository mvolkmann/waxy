package com.ociweb.xml;

import java.util.*;

/**
 * Each instance of this class represents the information about one
 * XML <code>Element</code>, its contents, and its context.
 * <p>
 *   Copyright (c) 2008, R. Mark Volkmann<br />
 *   All rights reserved.
 * </p>
 * <p>
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * </p>
 * <ul>
 *   <li>
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   </li>
 *   <li>
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   </li>
 *   <li>
 *     Neither the name of Object Computing, Inc. nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *   </li>
 * </ul>
 * <p>
 *   This software is provided by the copyright holders and contributors "as is"
 *   and any express or implied warranties, including, but not limited to,
 *   the implied warranties of merchantability and fitness for a particular
 *   purpose are disclaimed. In no event shall the copyright owner or
 *   contributors be liable for any direct, indirect, incidental, special,
 *   exemplary, or consequential damages (including, but not limited to,
 *   procurement of substitute goods or services; loss of use, data, or profits;
 *   or business interruption) however caused and on any theory of liability,
 *   whether in contract, strict liability, or tort (including negligence
 *   or otherwise) arising in any way out of the use of this software,
 *   even if advised of the possibility of such damage.
 * </p>
 * @author R. Mark Volkmann, Object Computing, Inc.
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
     * "qualified" (IE: namespace prefixed) attribute names. Cleared to empty at
     * the end of the start Element.
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

        definedAttributeNames.clear();
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
