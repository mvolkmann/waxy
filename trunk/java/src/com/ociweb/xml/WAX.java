package com.ociweb.xml;

import java.io.*;
import java.util.regex.Pattern;

/**
 * <p>
 *   This class provides methods that make outputting XML
 *   easy, fast and efficient in terms of memory utilization.
 * </p>
 * <p>A WAX object should not be used from multiple threads!</p>
 *
 * <p>For more information, see <a href="http://www.ociweb.com/wax/"
 *   target="_blank">http://www.ociweb.com/wax/</a>.</p>
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
public class WAX implements PrologOrElementWAX, StartTagWAX {

    public static final String MAC_LINE_SEPARATOR = "\n";
    public static final String UNIX_LINE_SEPARATOR = "\n";
    public static final String WINDOWS_LINE_SEPARATOR = "\r\n";

    /**
     * The current state of XML output is used to verify that methods
     * in this class aren't called in an illogical order.
     * If they are, an IllegalStateException is thrown.
     */
    private enum State { IN_PROLOG, IN_START_TAG, IN_ELEMENT, AFTER_ROOT }

    private final XMLWriter out;

    /**
     * Metadata about the XML Element we are currently writing. Has the value
     * <code>null</code> when outside the root element -- IE:
     * <code>state == IN_PROLOG</code> or <code>AFTER_ROOT</code>.
     */
    private ElementMetadata currentElementMetadata;

    /**
     * Holds DTD information for writing "<code>&lt;!DOCTYPE</code> ...
     * <code>&gt;</code>" document type declarations. Is <code>null</code> until
     * DTD or entity type information is specified, and is set back to
     * <code>null</code>, to release memory, when this information is written --
     * at the start of the root Element.
     */
    private DocType docType;

    private State state = State.IN_PROLOG;

    /**
     * <code>true</code> if the XML stylesheet has been specified, by writing an
     * "xml-stylesheet" processing instruction. This flag is used to prevent
     * writing more than one "xml-stylesheet" processing instruction to the
     * output stream.
     */
    private boolean xsltSpecified;

    private boolean verifyUsage = true;

    /**
     * Indicates whether to add a final newline to the output when closing.
     */
    private boolean addFinalNewline;

    /**
     * Creates a WAX that writes to stdout.
     */
    public WAX() { this(Version.UNSPECIFIED); }
    public WAX(Version version) {
        this(System.out, version);
        out.doNotCloseOutputStream(); // Don't close 'System.out'.
    }

    /**
     * Creates a WAX that writes to a given OutputStream.
     * The stream will be closed by the close method of this class.
     * @param os the OutputStream
     */
    public WAX(OutputStream os) { this(os, Version.UNSPECIFIED); }
    public WAX(OutputStream os, Version version) {
        this(new OutputStreamWriter(os), version);
    }

    /**
     * Creates a WAX that writes to a given file path.
     *
     * @param filePath the file path
     * @throws WAXIOException
     *             if the named file exists but is a directory rather than a
     *             regular file, does not exist but cannot be created, or cannot
     *             be opened for any other reason.
     */
    public WAX(String filePath) { this(filePath, Version.UNSPECIFIED); }
    public WAX(String filePath, Version version) {
        this(makeWriter(filePath), version);
    }

    /**
     * Creates a WAX that writes to a given Writer.
     * The writer will be closed by the close method of this class.
     * @param writer the Writer
     */
    public WAX(Writer writer) { this(writer, Version.UNSPECIFIED); }
    public WAX(Writer writer, Version version) {
        out = new XMLWriter(writer, verifyUsage);
        writeXMLDeclaration(version);
    }

    /**
     * Indicate that WAX should add a final newline when closing this WAX.
     * @return this WAX object.
     */
    public WAX includeFinalNewline() { addFinalNewline = true; return this; }

    /**
     * Indicate that WAX should not add a final newline when closing this WAX.
     * @return this WAX object.
     */
    public WAX excludeFinalNewline() { addFinalNewline = false; return this; }

    /**
     * Writes an attribute for the currently open element start tag.
     * @param name the attribute name
     * @param value the attribute value
     * @return the calling object to support chaining
     */
    public StartTagWAX attr(String name, Object value) {
        return attr(null, name, value);
    }

    /**
     * Writes an attribute for the currently open element start tag.
     * @param prefix the namespace prefix for the attribute
     * @param name the attribute name
     * @param value the attribute value
     * @return the calling object to support chaining
     */
    public StartTagWAX attr(String prefix, String name, Object value) {
        return attr(prefix, name, value, out.isAttrOnNewLine());
    }

    /**
     * Writes an attribute for the currently open element start tag.
     *
     * @param prefix the namespace prefix for the attribute
     * @param name the attribute name
     * @param value the attribute value
     * @param newLine true to write on a new line; false otherwise
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             unless we have a start tag open, for writing XML attributes.
     */
    public StartTagWAX attr(
        String prefix, String name, Object value, boolean newLine) {
        attr(prefix, name, value, newLine, true);
        return this;
    }

    private void attr(
        String prefix, String name, Object value,
        boolean newLine, boolean escape) {

        if (state != State.IN_START_TAG) badState("attr");

        currentElementMetadata.writeAttributeEqualsValue(prefix, name, value,
                newLine, escape);
    }

    /**
     * Throws an IllegalStateException that indicates the method that was called
     * and the current state that was invalid.
     *
     * @param methodName the method name
     * @throws IllegalStateException
     *             in all cases; exception message includes the
     *             <code>methodName</code> that was passed in.
     */
    private void badState(String methodName) {
        throw new IllegalStateException(
            "can't call " + methodName + " when state is " + state);
    }

    /**
     * Writes a blank line to increase readability of the XML.
     * @return the calling object to support chaining
     */
    public ElementWAX blankLine() {
        return text("", true);
    }

    /**
     * Writes a CDATA section in the content of the current element.
     * @param text the text
     * @return the calling object to support chaining
     */
    public ElementWAX cdata(String text) {
        return cdata(text, false);
    }

    /**
     * Writes a CDATA section in the content of the current element.
     *
     * @param text the text
     * @param newLine true to output the text on a new line; false otherwise
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             if before the beginning or after end of writing the root
     *             <code>Element</code>.
     */
    public ElementWAX cdata(String text, boolean newLine) {
        if (state == State.IN_PROLOG || state == State.AFTER_ROOT) {
            badState("cdata");
        }

        final String start = "<![CDATA[";
        final String end = "]]>";
        final String middle = text.replaceAll(
            Pattern.quote(end), "]]" + end + start + ">");
        text(start + middle + end, newLine, false);
        return this;
    }

    /**
     * A convenience method that is a shortcut for
     * start(name).end().
     * @param name the child element name
     * @return the calling object to support chaining
     */
    public ElementWAX child(String name) {
        return start(name).end();
    }

    /**
     * A convenience method that is a shortcut for
     * start(name).text(text).end().
     * @param name the child element name
     * @param text the child element text content
     * @return the calling object to support chaining
     */
    public ElementWAX child(String name, String text) {
        return child(null, name, text);
    }

    /**
     * A convenience method that is a shortcut for
     * start(prefix, name).text(text).end().
     *
     * @param prefix the namespace prefix of the child element
     * @param name the child element name
     * @param text the child element text content
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             if after end of writing the root <code>Element</code>.
     */
    public ElementWAX child(String prefix, String name, String text) {
        if (state == State.AFTER_ROOT) badState("child");
        return start(prefix, name).text(text).end();
    }

    /**
     * Terminates all unterminated elements, closes the Writer that is being
     * used to output XML, and insures that nothing else can be written.
     *
     * @throws IllegalStateException
     *             if WAX had already been closed, or
     *             if we have not yet written a root <code>Element</code>.
     * @throws WAXIOException if an I/O error occurs.
     */
    public void close() {
        if (out.isClosed()) throw new IllegalStateException("already closed");

        // Verify that a root element has been written and not yet ended.
        if (state == State.IN_PROLOG) badState("close");

        // End all the unended elements.
        while (currentElementMetadata != null) {
            end();
        }

        if ( addFinalNewline ) { out.writeln(); }

        out.close();
    }

    /**
     * Closes the start tag, with &gt; or /&gt;, that had been kept open
     * waiting for more namespace declarations and attributes.
     */
    private void closeStartTag() {
        verifyOutstandingNamespacePrefixes();
        if (state != State.IN_START_TAG) return;

        currentElementMetadata.closeStartTag();
        state = State.IN_ELEMENT;
    }

    /**
     * Writes a comment (&lt;!-- text --&gt;).
     * @param text the comment text (cannot contain "--")
     * @return the calling object to support chaining
     */
    public PrologOrElementWAX comment(String text) {
        return comment(text, false);
    }

    /**
     * Writes a comment (&lt;!-- text --&gt;).
     *
     * @param text the comment text (cannot contain "--")
     * @param newLine true to output the text on a new line; false otherwise
     * @return the calling object to support chaining
     * @throws IllegalArgumentException
     *             if <code>text</code> is <code>null</code> or contains two
     *             sequential dash characters (<code>"--"</code>).
     */
    public PrologOrElementWAX comment(String text, boolean newLine) {
        // Comments can be output in any state.
        if (verifyUsage) XMLUtil.verifyComment(text);

        closeStartTag();
        out.writeComment(text, newLine);
        return this;
    }

    /**
     * Writes a commented start tag for a given element name,
     * but doesn't terminate it.
     * @param name the element name
     * @return the calling object to support chaining
     */
    public StartTagWAX commentedStart(String name) {
        return commentedStart(null, name);
    }

    /**
     * Writes a commented start tag for a given element name,
     * but doesn't terminate it.
     * @param prefix the namespace prefix to used on the element
     * @param name the element name
     * @return the calling object to support chaining
     */
    public StartTagWAX commentedStart(String prefix, String name) {
        start(prefix, name, true);
        return this;
    }

    /**
     * Writes a namespace declaration for the default namespace
     * in the start tag of the current element.
     * @param uri the namespace URI
     * @return the calling object to support chaining
     */
    public StartTagWAX defaultNamespace(String uri) {
        return namespace("", uri);
    }

    /**
     * Writes a namespace declaration for the default namespace
     * in the start tag of the current element.
     * @param uri the namespace URI
     * @param schemaPath the path to the XML Schema
     * @return the calling object to support chaining
     */
    public StartTagWAX defaultNamespace(String uri, String schemaPath) {
        return namespace("", uri, schemaPath);
    }

    /**
     * Shorthand name for the defaultNamespace method.
     * @see #defaultNamespace(String)
     * @param uri the namespace URI
     * @return the calling object to support chaining
     */
    public StartTagWAX defaultNS(String uri) {
        return defaultNamespace(uri);
    }

    /**
     * Shorthand name for the defaultNamespace method.
     * @see #defaultNamespace(String, String)
     * @param uri the namespace URI
     * @param schemaPath the path to the XML Schema
     * @return the calling object to support chaining
     */
    public StartTagWAX defaultNS(String uri, String schemaPath) {
        return defaultNamespace(uri, schemaPath);
    }

    /**
     * Writes a DOCTYPE that associates a DTD with the XML document.
     * @param systemId the file path or URL to the DTD
     * @return the calling object to support chaining
     */
    public PrologWAX dtd(String systemId) {
        return dtd(null, systemId);
    }

    /**
     * Writes a DOCTYPE that associates a DTD with the XML document.
     *
     * @param publicId the public ID of the DTD
     * @param systemId the file path or URL to the DTD
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             if this method is called more than once, or
     *             if we have already started writing XML <code>Element</code>s.
     * @throws IllegalArgumentException
     *             if <code>systemId</code> is not a valid URI.
     */
    public PrologWAX dtd(String publicId, String systemId) {
        if (docType != null) {
            throw new IllegalStateException("can't specify more than one DTD");
        }

        if (state != State.IN_PROLOG) badState("dtd");

        if (systemId == null) {
            throw new IllegalArgumentException(
                    "DTD 'system identifier' parameter must not be null.");
        }

        if (verifyUsage) XMLUtil.verifyURI(systemId);

        docType = new DocType(publicId, systemId);
        return this;
    }

    /**
     * Terminates the current element.
     * It does so in the shorthand way (/&gt;) if the element has no content,
     * and in the long way (&lt;/name&gt;) if it does.
     * @return the calling object to support chaining
     */
    public ElementWAX end() {
        return end(false);
    }

    /**
     * Terminates the current element.
     * It does so in the shorthand way (/&gt;)
     * if the element has no content AND false is passed.
     * Otherwise it does so in the long way (&lt;/name&gt;).
     * The verbose option is useful in cases like the HTML script tag
     * which cannot be terminated in the shorthand way
     * even though it has no content.
     *
     * @param verbose true to not consider shorthand way; false to consider it
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             if before the beginning or after end of writing the root
     *             <code>Element</code>.
     */
    public ElementWAX end(boolean verbose) {
        if (state == State.IN_PROLOG || state == State.AFTER_ROOT) {
            badState("end");
        }

        if (state == State.IN_START_TAG) {
            verifyOutstandingNamespacePrefixes();
        }

        currentElementMetadata.writeEndTag(verbose);

        currentElementMetadata = currentElementMetadata.getParent();
        state = currentElementMetadata == null ?
            State.AFTER_ROOT : State.IN_ELEMENT;
        return this;
    }

    /**
     * Adds an entity definition to the internal subset of the DOCTYPE.
     *
     * @param name
     * @param value
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             if we have already started writing XML <code>Element</code>s.
     */
    public PrologWAX entityDef(String name, String value) {
        if (state != State.IN_PROLOG) badState("entity");

        if (docType == null) docType = new DocType(null, null);
        docType.entityDef(name, value);
        return this;
    }

    /**
     * Adds an external entity definition to the internal subset of the DOCTYPE.
     * @param name the name
     * @param filePath the filePath
     * @return the calling object to support chaining
     */
    public PrologWAX externalEntityDef(String name, String filePath) {
        return entityDef(name + " SYSTEM", filePath);
    }

    /**
     * Gets the indentation characters being used.
     * Note that there is a distinction between null and "".
     * @see #setIndent(String)
     * @see #setIndent(int)
     * @return the indentation characters
     */
    public String getIndent() {
        return out.getIndent();
    }

    /**
     * Gets the line separator characters currently being used.
     * @return the line separator characters
     */
    public String getLineSeparator() {
        return out.getLineSeparator();
    }

    /**
     * Gets the part of the xsi namespace URI that specifies
     * the version of XML Schema being used.
     * Typically it will be "1999" or "2001"
     */
    public String getSchemaVersion() {
        return out.getSchemaVersion();
    }

    /**
     * Indicates whether a space is added before the slash in empty elements.
     * @see #setSpaceInEmptyElements(boolean)
     * @return true if a space is added; false otherwise
     */
    public boolean isSpaceInEmptyElements() {
        return out.isSpaceInEmptyElements();
    }

    /**
     * Gets whether "trust me" mode is enabled.
     * @see #setTrustMe
     * @return true if error checking is disabled; false if enabled
     */
    public boolean isTrustMe() {
        return !verifyUsage;
    }

    /**
     * Creates a Writer for a given file path.
     *
     * @param filePath
     *            the file path
     * @return the Writer
     * @throws WAXIOException
     *             if the named file exists but is a directory rather than a
     *             regular file, does not exist but cannot be created, or cannot
     *             be opened for any other reason.
     */
    private static Writer makeWriter(String filePath) {
        try {
            return new FileWriter(filePath);
        } catch (IOException ioException) {
            throw new WAXIOException(ioException);
        }
    }

    /**
     * Writes a namespace declaration in the start tag of the current element.
     * To define the default namespace, use one of the defaultNamespace methods.
     * @param prefix the namespace prefix
     * @param uri the namespace URI
     * @return the calling object to support chaining
     */
    public StartTagWAX namespace(String prefix, String uri) {
        return namespace(prefix, uri, null);
    }

    /**
     * Writes a namespace declaration in the start tag of the current element.
     * To define the default namespace, use one of the defaultNamespace methods.
     *
     * @param prefix the namespace prefix
     * @param uri the namespace URI
     * @param schemaPath the path to the XML Schema
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             unless we have a start tag open, for writing XML attributes.
     * @throws IllegalArgumentException
     *             if <code>prefix</code> is not a valid XML name token, or
     *             if <code>uri</code> or <code>schemaPath</code> are not a
     *             valid URIs, or
     *             when one attempts to define the same namespace
     *             <code>prefix</code> more than once within the same start XML
     *             <code>Element</code>.
     */
    public StartTagWAX namespace(
        String prefix, String uri, String schemaPath) {

        if (state != State.IN_START_TAG) badState("namespace");

        currentElementMetadata.writeNamespaceDeclaration(
            prefix, uri, schemaPath);
        return this;
    }

    /**
     * Shorthand name for the namespace method.
     * @see #namespace(String, String)
     * @param prefix the namespace prefix
     * @param uri the namespace URI
     * @return the calling object to support chaining
     */
    public StartTagWAX ns(String prefix, String uri) {
        return namespace(prefix, uri);
    }

    /**
     * Shorthand name for the namespace method.
     * @see #namespace(String, String, String)
     * @param prefix the namespace prefix
     * @param uri the namespace URI
     * @param schemaPath the path to the XML Schema
     * @return the calling object to support chaining
     */
    public StartTagWAX ns(
        String prefix, String uri, String schemaPath) {
        return namespace(prefix, uri, schemaPath);
    }

    /**
     * Shorthand name for the processingInstruction method.
     * @see #pi(String, String)
     * @param target the processing instruction target
     * @param data the processing instruction data
     * @return the calling object to support chaining
     */
    public PrologOrElementWAX pi(String target, String data) {
        return processingInstruction(target, data);
    }

    /**
     * Writes a processing instruction.
     *
     * @param target the processing instruction target
     * @param data the processing instruction data
     * @return the calling object to support chaining
     * @throws IllegalArgumentException
     *             if <code>target</code> is not a valid XML name token.
     */
    public PrologOrElementWAX processingInstruction(
        String target, String data) {
        // Processing instructions can go anywhere
        // except inside element start tags and attribute values.

        // Provide special handling for the
        // "xml-stylesheet" processing instruction
        // since starting with "xml" is reserved.
        if (verifyUsage && !("xml-stylesheet").equals(target)) {
            XMLUtil.verifyName(target);
        }

        closeStartTag();
        out.writeProcessingInstruction(target, data);
        return this;
    }

    /**
     * Sets the indentation characters to use.
     * This defaults to two spaces.
     * Unless "trust me" is set to true, the only valid values are
     * a single tab, one or more spaces, an empty string, or null.
     * Passing "" causes elements to be output on separate lines,
     * but not indented.
     * Passing null causes all output to be on a single line.
     *
     * @throws IllegalArgumentException
     *             if the <code>indent</code> string does not follow the rules
     *             above
     *             <i>(and the "trust me" flag isn't <code>true</code>)</i>.
     */
    public void setIndent(String indent) {
        out.setIndent(indent);
    }

    /**
     * Sets the number of spaces to use for indentation.
     * The number must be >= 0 and <= 4.
     * This defaults to 2.
     *
     * @param numSpaces the number of spaces
     * @throws IllegalArgumentException
     *             if <code>numSpaces</code> is negative or more than four.
     */
    public void setIndent(int numSpaces) {
        out.setIndent(numSpaces);
    }

    /**
     * Don't output indent output or write line separators.
     * Write out the XML on a single line.
     */
    public void noIndentsOrLineSeparators() {
        setIndent(null);
    }

    /**
     * Sets the line separator characters to be used.
     *
     * @param lineSeparator the line separator characters
     * @throws IllegalStateException
     *             if data has already been written.
     * @throws IllegalArgumentException
     *             if the <code>lineSeparator</code> sequence is not one of the
     *             recognized (Mac, Unix or Windows) line terminator/separator
     *             character sequences.
     */
    public void setLineSeparator(String lineSeparator) {
        out.setLineSeparator(lineSeparator);
    }

    /**
     * Sets the part of the xsi namespace URI that specifies
     * the version of XML Schema being used.
     * @param version typically "1999" or "2001"
     */
    public void setSchemaVersion(String version) {
        out.setSchemaVersion(version);
    }

    /**
     * Sets whether a space will be added before the closing slash
     * in empty elements.
     * When set true, output will look like "<tag />".
     * When set false, output will look like "<tag/>".
     * @see #isSpaceInEmptyElements()
     * @param spaceInEmptyElements true to include a space; false otherwise
     */
    public void setSpaceInEmptyElements(boolean spaceInEmptyElements) {
        out.setSpaceInEmptyElements(spaceInEmptyElements);
    }

    /**
     * Sets whether "trust me" mode is enabled.
     * When disabled (the default), the following checks are made.
     * 1) element names, attribute names, namespace prefixes and
     *    processing instruction targets are verified to be valid XML names
     * 2) comments are verified to not contain "--"
     * 3) element and attribute prefixes are verified to be in scope
     * 4) DTD paths, namespaces, schema paths and XSLT paths
     *    are to use valid URI syntax
     * 5) only sensible indent values (none, two spaces, four spaces or one tab)
     *    are allowed (can use other values if trustMe = true)
     * The main reason to enable "trust me" mode is for performance
     * which is typically good even when disabled.
     * @see #isTrustMe
     * @param trustMe true to disable error checking; false to enable it
     */
    public void setTrustMe(boolean trustMe) {
        this.verifyUsage = !trustMe;
        out.setTrustMe(trustMe);
        if (currentElementMetadata != null)
            currentElementMetadata.setTrustMe(trustMe);
    }

    /**
     * Writes the start tag for a given element name, but doesn't terminate it.
     * @param name the element name
     * @return the calling object to support chaining
     */
    public StartTagWAX start(String name) {
        return start(null, name);
    }

    /**
     * Writes the start tag for a given element name, but doesn't terminate it.
     *
     * @param prefix the namespace prefix to used on the element
     * @param name the element name
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             if the root XML <code>Element</code> has been closed.
     */
    public StartTagWAX start(String prefix, String name) {
        start(prefix, name, false);
        return this;
    }

    private void start(String prefix, String name, boolean inCommentedStart) {
        closeStartTag();
        out.resetContentFlags();

        if (state == State.AFTER_ROOT) badState("start");

        final boolean isTheRootElement = (currentElementMetadata == null);
        if (isTheRootElement) writeDocType(name);

        currentElementMetadata = new ElementMetadata(
            out, verifyUsage, currentElementMetadata,
            prefix, name, inCommentedStart);
        currentElementMetadata.writeStartTagOpen(inCommentedStart);

        state = State.IN_START_TAG;
    }

    /**
     * Writes text inside the content of the current element.
     * @param text the text
     * @return the calling object to support chaining
     */
    public ElementWAX text(String text) {
        return text(text, false);
    }

    /**
     * Writes text inside the content of the current element.
     *
     * @param text the text
     * @param newLine true to output the text on a new line; false otherwise
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             if before the beginning or after end of writing the root
     *             <code>Element</code>.
     */
    public ElementWAX text(String text, boolean newLine) {
        text(text, newLine, true);
        return this;
    }

    private void text(String text, boolean newLine, boolean escape) {
        if (state == State.IN_PROLOG || state == State.AFTER_ROOT) {
            badState("text");
        }

        closeStartTag();
        out.writeText(text, newLine, escape);
    }

    /**
     * Same as the attr method, but special characters in the value
     * aren't escaped.  This allows entity references to be embedded.
     * @see #attr(String, Object)
     * @param name the attribute name
     * @param value the attribute value
     * @return the calling object to support chaining
     */
    public StartTagWAX unescapedAttr(String name, Object value) {
        return unescapedAttr("", name, value);
    }

    /**
     * Same as the attr method, but special characters in the value
     * aren't escaped.  This allows entity references to be embedded.
     * @see #attr(String, String, Object)
     * @param prefix the namespace prefix for the attribute
     * @param name the attribute name
     * @param value the attribute value
     * @return the calling object to support chaining
     */
    public StartTagWAX unescapedAttr(String prefix, String name, Object value) {
        return unescapedAttr(prefix, name, value, false);
    }

    /**
     * Same as the attr method, but special characters in the value
     * aren't escaped.  This allows entity references to be embedded.
     * @see #attr(String, String, Object, boolean)
     * @param prefix the namespace prefix for the attribute
     * @param name the attribute name
     * @param value the attribute value
     * @param newLine true to write on a new line; false otherwise
     * @return the calling object to support chaining
     */
    public StartTagWAX unescapedAttr(
        String prefix, String name, Object value, boolean newLine) {
        attr(prefix, name, value, newLine, false);
        return this;
    }

    /**
     * Same as the text method, but special characters in the value
     * aren't escaped.  This allows entity references to be embedded.
     * @see #unescapedText(String)
     * @param text the text
     * @return the calling object to support chaining
     */
    public ElementWAX unescapedText(String text) {
        return unescapedText(text, false);
    }

    /**
     * Same as the text method, but special characters in the value
     * aren't escaped.  This allows entity references to be embedded.
     * @see #unescapedText(String, boolean)
     * @param text the text
     * @param newLine true to output the text on a new line; false otherwise
     * @return the calling object to support chaining
     */
    public ElementWAX unescapedText(String text, boolean newLine) {
        text(text, newLine, false);
        return this;
    }

    private void verifyOutstandingNamespacePrefixes() {
        if (verifyUsage && currentElementMetadata != null) {
            currentElementMetadata.verifyOutstandingNamespacePrefixes();
        }
    }

    /**
     * Writes a DOCTYPE.
     * @param rootElementName the root element name
     */
    private void writeDocType(String rootElementName) {
        if (docType != null) {
            docType.write(out, rootElementName);
            docType = null; // release memory
        }
    }

    /**
     * Writes an XML declaration.
     * Regardless of indentation,
     * a newline is always written after this.
     *
     * @param version the XML version
     * @throws IllegalArgumentException
     *             if <code>version</code> is <code>null</code>.
     */
    private void writeXMLDeclaration(Version version) {
        if (version == null) {
            throw new IllegalArgumentException("unsupported XML version");
        }

        if (version != Version.UNSPECIFIED) {
            out.writeXMLDeclaration(version.getVersionNumberString());
        }
    }

    /**
     * Writes an "xml-stylesheet" processing instruction.
     *
     * @param filePath the path to the XSLT stylesheet
     * @return the calling object to support chaining
     * @throws IllegalStateException
     *             if a stylesheet has already been specified, or
     *             if we have already started writing XML <code>Element</code>s.
     * @throws IllegalArgumentException
     *             if <code>filePath</code> is not a valid URI.
     */
    public PrologWAX xslt(String filePath) {
        if (xsltSpecified) {
            throw new IllegalStateException("can't specify more than one XSLT");
        }

        if (state != State.IN_PROLOG) badState("xslt");

        if (verifyUsage) XMLUtil.verifyURI(filePath);

        xsltSpecified = true;
        return processingInstruction(
            "xml-stylesheet", "type=\"text/xsl\" href=\"" + filePath + "\"");
    }
}
