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
     * <code>true</code> when the default namespace has been defined in this XML
     * <code>Element</code>. <i>Is closely related to the
     * <code>namespacePrefixToURLMap</code> field.</i>
     */
    private boolean defaultNamespaceDefined = false;

    /**
     * Set of all attribute names defined in this XML Element. Contains all
     * "qualified" (IE: namespace prefixed) attribute names.
     * <p>
     * Lifetime: Valid while <code>(WAX.state == State.IN_START_TAG)</code>.
     * Cleared to empty at the end of the start Element.
     * </p>
     */
    private final Set<String> definedAttributeNames = new HashSet<String>();

    /**
     * <code>true</code> if and only if this XML Element is the start/root/base
     * of an XML Fragment that is a comment representing possible XML elements.
     */
    private final boolean isCommentElement;

    /**
     * XML Namespace prefixes defined in this XML Element. <i>(...except for the
     * default namespace.)</i>
     * <p>
     * Lifetime: This value is used by this and child classes. It's lifetime is
     * the same as this class.
     * </p>
     */
    private final Map<String, String> namespacePrefixToURLMap = new HashMap<String, String>();

    private final XMLWriter out;

    /**
     * <code>ElementMetadata</code> for the XML Element that contains this
     * Element in the XML output stream.
     */
    private final ElementMetadata parent;

    /**
     * XML <a href="http://www.w3.org/TR/2008/PER-xml-20080205/#NT-Name">
     * <code>Name</code></a> of an XML <code>Element</code>, with optional XML
     * Namespace "colon" prefix.
     * <p>
     * Lifetime: This value is used to open and close the XML Element. It's
     * lifetime is the same as this class.
     * </p>
     */
    private final String qualifiedName;

    /**
     * A <code>Map</code> of namespace URI strings to the schema path that would
     * validate each. This map is used and contains useful data for the duration
     * of a start tag -- IE: when <code>state == IN_START_TAG</code>. It is
     * cleared at the end of each start tag.
     * <p>
     * Implementation Note: A TreeMap is used so that the 'xsi:schemaLocation'
     * will be written in sorted order.
     * </p>
     * <p>
     * Lifetime: Valid while <code>(WAX.state == State.IN_START_TAG)</code>.
     * Cleared to empty at the end of the start Element.
     * </p>
     */
    private final Map<String, String> namespaceURIToSchemaPathMap = new TreeMap<String, String>();

    private boolean verifyUsage;

    /* package */ ElementMetadata(final XMLWriter out,
        final boolean verifyUsage, final ElementMetadata parent,
        final String prefix, final String name, final boolean isCommentElement) {
        this.verifyUsage = verifyUsage;
        this.out = out;

        this.qualifiedName = buildQualifiedName(prefix, name);
        this.isCommentElement = isCommentElement;
        this.parent = parent;
    }

    /**
     * Build <a href="http://www.w3.org/TR/REC-xml-names/">XML Namespace</a> <a
     * href="http://www.w3.org/TR/REC-xml-names/#NT-QName">"Qualified Name"</a>
     * for an XML element or attribute.
     * 
     * @param prefix
     *            the namespace prefix for the attribute
     * @param name
     *            the element or attribute name
     * @return <a href="http://www.w3.org/TR/REC-xml-names/">XML Namespace</a>
     *         <a href="http://www.w3.org/TR/REC-xml-names/#NT-QName">
     *         "Qualified Name"</a>
     */
    public String buildQualifiedName(final String prefix, final String name) {
        final boolean hasPrefix = XMLUtil.hasValue(prefix);

        if (verifyUsage) {
            if (hasPrefix)
                XMLUtil.verifyName(prefix);
            XMLUtil.verifyName(name);
        }

        return (hasPrefix) ? (prefix + ':' + name) : name;
    }

    public void closeStartTag() {
        writeSchemaLocations();
        out.writeStartTagClose();
    }

    public boolean containsNamespacePrefix(final String prefix) {
        return namespacePrefixToURLMap.containsKey(prefix);
    }

    private String formatSchemaLocationString() {
        final StringBuilder schemaLocation = new StringBuilder();
        for (final String uri : namespaceURIToSchemaPathMap.keySet()) {
            final String path = namespaceURIToSchemaPathMap.get(uri);

            // If not the first pair output ...
            if (schemaLocation.length() > 0)
                schemaLocation.append(out.getWhiteSpaceBreakForChildLevel());

            schemaLocation.append(uri + ' ' + path);
        }

        return schemaLocation.toString();
    }

    /**
     * Get the URL string value for the given namespace prefix in the current
     * scope. Returns <code>null</code> if undefined.
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

    public ElementMetadata getParent() {
        return parent;
    }

    /**
     * @param prefix
     * @return The URL for the given namespace <code>prefix</code>.
     * @throws IllegalArgumentException
     *             if the namespace <code>prefix</code> is not defined in the
     *             current scope.
     */
    private String getRequiredNamespaceURL(final String prefix) {
        final String namespaceURL = getNamespaceUrl(prefix);
        if (namespaceURL == null || "".equals(namespaceURL)) {
            throw new IllegalArgumentException("The namespace prefix \""
                + prefix + "\" isn't in scope.");
        }
        return namespaceURL;
    }

    public void setTrustMe(final boolean trustMe) {
        this.verifyUsage = !trustMe;
        if (parent != null)
            parent.setTrustMe(trustMe);
    }

    private void verifyAttributeNamesWithinStartTag() {
        final Set<String> expandedAttributeNames = new HashSet<String>();

        for (final String qualifiedAttributeName : definedAttributeNames) {
            final int colonIndex = qualifiedAttributeName.indexOf(':');
            if (colonIndex > 0) {
                final String prefix = qualifiedAttributeName.substring(0,
                    colonIndex);
                final String name = qualifiedAttributeName
                    .substring(colonIndex + 1);
                final String namespaceURL = getRequiredNamespaceURL(prefix);

                final String expandedName = namespaceURL + ':' + name;
                if (expandedAttributeNames.contains(expandedName)) {
                    throw new IllegalArgumentException(
                        "The attribute <xmlns:ns=\"" + namespaceURL + "\" ns:"
                            + name + "> is defined twice in this element.");
                } else {
                    expandedAttributeNames.add(expandedName);
                }
            }
        }

        definedAttributeNames.clear();
    }

    private void verifyElementNamespaceUsage() {
        final int colonIndex = qualifiedName.indexOf(':');
        if (colonIndex > 0) {
            final String prefix = qualifiedName.substring(0, colonIndex);
            getRequiredNamespaceURL(prefix);
        }
    }

    private void verifyNamespaceData(final String prefix, final String uri,
        final String schemaPath) {
        final boolean hasPrefix = XMLUtil.hasValue(prefix);
        if (hasPrefix) XMLUtil.verifyName(prefix);
        XMLUtil.verifyURI(uri);
        if (schemaPath != null) XMLUtil.verifyURI(schemaPath);

        // Verify that the prefix isn't already defined
        // on the current element.
        if (hasPrefix) {
            if (containsNamespacePrefix(prefix)) {
                throw new IllegalArgumentException("The namespace prefix \""
                    + prefix + "\" is already defined on the current element.");
            }
        } else if (defaultNamespaceDefined) {
            throw new IllegalArgumentException("The default namespace "
                + "is already defined on the current element.");
        }
    }

    /**
     * Verifies that all the pending namespace prefix are currently in scope.
     * 
     * @throws IllegalArgumentException if any aren't in scope
     */
    public void verifyOutstandingNamespacePrefixes() {
        verifyElementNamespaceUsage();
        verifyAttributeNamesWithinStartTag();
    }

    public String writeAttributeEqualsValue(final String prefix,
        final String name, final Object value, final boolean newLine,
        final boolean escape) {
        final String qualifiedAttributeName = buildQualifiedName(prefix, name);
        if (definedAttributeNames.contains(qualifiedAttributeName)) {
            throw new IllegalArgumentException("The attribute \""
                + qualifiedAttributeName
                + "\" is defined twice in this element.");
        }

        definedAttributeNames.add(qualifiedAttributeName);

        out.writeAttributeEqualsValue(qualifiedAttributeName, value, newLine,
            escape);

        return qualifiedAttributeName;
    }

    public void writeEndTag(final boolean verbose) {
        writeSchemaLocations();
        out.writeEndTag(qualifiedName, isCommentElement, verbose);
    }

    public void writeNamespaceDeclaration(final String prefix,
        final String uri, final String schemaPath) {
        if (verifyUsage) {
            verifyNamespaceData(prefix, uri, schemaPath);
        }

        out.writeNamespaceDeclaration(prefix, uri);

        // Add this prefix to the list of those in scope for this element.
        final boolean hasPrefix = XMLUtil.hasValue(prefix);
        if (hasPrefix) {
            namespacePrefixToURLMap.put(prefix, uri);
        } else {
            defaultNamespaceDefined = true;
        }

        if (schemaPath != null) {
            namespaceURIToSchemaPathMap.put(uri, schemaPath);
        }
    }

    /**
     * Writes the namespace declaration for the XMLSchema-instance namespace and
     * writes the schemaLocation attribute which associates namespace URIs with
     * schema locations.
     */
    private void writeSchemaLocations() {
        if (namespaceURIToSchemaPathMap.isEmpty())
            return;

        // Write the attributes needed to associate XML Schemas
        // with this XML.
        writeNamespaceDeclaration("xsi", XMLUtil.XMLSCHEMA_INSTANCE_NS, null);
        writeAttributeEqualsValue("xsi", "schemaLocation",
            formatSchemaLocationString(), out.isIndentDefined(), false);

        namespaceURIToSchemaPathMap.clear();
    }

    /**
     * Write the opening and name portion of a start tag.
     */
    public void writeStartTagOpen(final boolean inCommentedStart) {
        out.writeStartTagOpen(qualifiedName, inCommentedStart);
    }

}
