package com.ociweb.xml;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>
 * This class provides methods that make outputting XML
 * easy, fast and efficient in terms of memory utilization.
 * </p>
 * <p>A WAX object should not be used from multiple threads!</p>
 * 
 * For more information, see <a href="http://www.ociweb.com/wax/"
 * target="_blank">http://www.ociweb.com/wax/</a>.
 * </p>
 * <p>Copyright 2008 R. Mark Volkmann</p>
 * <p>This file is part of WAX.</p>
 * <p>
 * WAX is free software.  You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * </p>
 * <p>
 * WAX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * </p>
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with WAX.  If not, see http://www.gnu.org/licenses.
 * </p>
 * 
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

    private final List<String> entityDefs = new ArrayList<String>();

    // Using a TreeMap so keys are kept in sorted order.
    private final Map<String, String> namespaceURIToSchemaPathMap =
        new TreeMap<String, String>();

    private final Stack<ElementMetadata> elementStack =
        new Stack<ElementMetadata>();

    private State state = State.IN_PROLOG;
    private String doctypePublicId;
    private String doctypeSystemId;
    private String indent = "  ";
    private String lineSeparator;
    private Writer writer;
    private boolean attrOnNewLine;
    private boolean checkMe = true;
    private boolean closeStream = true;
    private boolean dtdSpecified;
    private boolean escape = true;
    private boolean hasContent;
    private boolean hasIndentedContent;
    private boolean inCommentedStart;
    private boolean outputStarted;
    private boolean spaceInEmptyElements;
    private boolean xsltSpecified;

    /**
     * Creates a WAX that writes to stdout.
     */
    public WAX() { this(Version.UNSPECIFIED); }
    public WAX(Version version) {
        this(System.out, version);
        closeStream = false;
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
        this.writer = writer;
        lineSeparator = System.getProperty("line.separator");

        writeXMLDeclaration(version);
    }

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
        return attr(prefix, name, value, attrOnNewLine);
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

        if (state != State.IN_START_TAG) badState("attr");

        ElementMetadata currentElementMetadata = elementStack.peek();
        String qualifiedName = currentElementMetadata.defineAttribute(
                prefix, name, checkMe);

        if (newLine) {
            writeIndent();
        } else {
            write(' ');
        }

        if (escape) value = XMLUtil.escape(value);

        write(qualifiedName + "=\"" + value + "\"");

        return this;
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

        escape = false;
        final String start = "<![CDATA[";
        final String end = "]]>";
        final String middle = text.replaceAll(Pattern.quote(end),
                "]]" + end + start + ">");
        text(start + middle + end, newLine);
        escape = true;
        return this;
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
        if (writer == null) throw new IllegalStateException("already closed");

        // Verify that a root element has been written.
        if (state == State.IN_PROLOG) badState("close");

        // End all the unended elements.
        while (elementStack.size() > 0) end();

        try {
            if (closeStream) {
                writer.close();
            } else {
                writer.flush();
            }
        } catch (IOException ioException) {
            throw new WAXIOException(ioException);
        }

        writer = null;
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

        if (checkMe) XMLUtil.verifyComment(text);
        
        hasContent = hasIndentedContent = true;
        terminateStart();
        if (elementStack.size() > 0) writeIndent();

        if (newLine && willIndent()) {
            write("<!--");
            writeIndent();
            write(indent);
            write(text);
            writeIndent();
            write("-->");
        } else {
            write("<!-- ");
            write(text);
            write(" -->");
        }

        if (willIndent() && elementStack.empty()) write(lineSeparator);

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
        inCommentedStart = true;
        start(prefix, name);
        inCommentedStart = false;
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
        if (dtdSpecified) {
            throw new IllegalStateException("can't specify more than one DTD");
        }
        if (state != State.IN_PROLOG) badState("dtd");
        if (checkMe) XMLUtil.verifyURI(systemId);

        doctypePublicId = publicId;
        doctypeSystemId = systemId;
        dtdSpecified = true;
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

        verifyOutstandingNamespacePrefixes();

        writeSchemaLocations();

        ElementMetadata elementMetadata = elementStack.pop();

        if (hasContent || verbose) {
            if (verbose) write('>');
            if (hasIndentedContent) writeIndent();
            write("</");
            write(elementMetadata.getQualifiedName());
        } else {
            if (spaceInEmptyElements) write(' ');
            write('/');
        }
        write(elementMetadata.isCommentElement() ? "-->" : ">");

        hasContent = hasIndentedContent = true; // new setting for parent

        state = elementStack.empty() ? State.AFTER_ROOT : State.IN_ELEMENT;

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
        entityDefs.add(name + " \"" + value + '"');
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
        return indent;
    }

    /**
     * Gets the line separator characters currently being used.
     * @return the line separator characters
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Indicates whether a space is added before the slash in empty elements.
     * @see #setSpaceInEmptyElements(boolean)
     * @return true if a space is added; false otherwise
     */
    public boolean isSpaceInEmptyElements() {
        return spaceInEmptyElements;
    }

    /**
     * Gets whether "trust me" mode is enabled.
     * @see #setTrustMe
     * @return true if error checking is disabled; false if enabled
     */
    public boolean isTrustMe() {
        return !checkMe;
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

        ElementMetadata currentElementMetadata = elementStack.peek();

        if (checkMe) {
            currentElementMetadata.verifyNamespaceData(prefix, uri, schemaPath);
        }

        if (willIndent()) {
            writeIndent();
        } else {
            write(' ');
        }
        
        write("xmlns");
        if (XMLUtil.hasValue(prefix)) write(':' + prefix);
        write("=\"" + uri + "\"");
        
        if (schemaPath != null) {
            namespaceURIToSchemaPathMap.put(uri, schemaPath);
        }

        // Add this prefix to the list of those in scope for this element.
        currentElementMetadata.defineNamespace(prefix, uri);

        attrOnNewLine = true; // for the next attribute

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

        if (checkMe) {
            // Processing instructions can go anywhere
            // except inside element start tags and attribute values.

            // Provide special handling for the
            // "xml-stylesheet" processing instruction
            // since starting with "xml" is reserved.
            if (!target.equals("xml-stylesheet")) XMLUtil.verifyName(target);
        }
        
        hasContent = hasIndentedContent = true;
        terminateStart();
        if (elementStack.size() > 0) writeIndent();

        write("<?" + target + ' ' + data + "?>");
        if (willIndent() && elementStack.empty()) write(lineSeparator);

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
        if (checkMe) {
            boolean valid =
                indent == null || indent.length() == 0 || "\t".equals(indent);
            
            if (!valid) {
                // It can only be valid now if every character is a space.
                valid = true; // assume
                for (int i = 0; i < indent.length(); ++i) {
                    if (indent.charAt(i) != ' ') {
                        valid = false;
                        break;
                    }
                }
            }
            
            if (!valid || (indent != null && indent.length() > 4)) {
                throw new IllegalArgumentException("invalid indent value");
            }
        }
        
        this.indent = indent;
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
        if (numSpaces < 0) {
            throw new IllegalArgumentException(
                "can't indent a negative number of spaces");
        }

        if (checkMe && numSpaces > 4) {
            throw new IllegalArgumentException(
                numSpaces + " is an unreasonable indentation");
        }

        indent = "";
        for (int i = 0; i < numSpaces; i++) indent += ' ';
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
        if (outputStarted) {
            throw new IllegalStateException(
                "can't change CR characters after output has started");
        }

        if (checkMe) {
            boolean valid =
                MAC_LINE_SEPARATOR.equals(lineSeparator) ||
                UNIX_LINE_SEPARATOR.equals(lineSeparator) ||
                WINDOWS_LINE_SEPARATOR.equals(lineSeparator);
            if (!valid) {
                throw new IllegalArgumentException(
                    "invalid line separator characters");
            }
        }

        this.lineSeparator = lineSeparator;
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
        this.spaceInEmptyElements = spaceInEmptyElements;
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
        this.checkMe = !trustMe;
    }

    /**
     * @param elementStack Possibly empty <code>Stack</code>, which will
     *     <b>not</b> be modified.
     * @return the object at the top of this stack, if there is one, else
     *     <code>null</code>.
     */
    private static ElementMetadata softPeek(Stack<ElementMetadata> elementStack) {
        if (elementStack.empty())
            return null;
        else
            return elementStack.peek();
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
        hasContent = hasIndentedContent = true;
        terminateStart();
        hasContent = hasIndentedContent = false;

        if (state == State.AFTER_ROOT) badState("start");

        ElementMetadata elementMetadata = new ElementMetadata(prefix, name,
                inCommentedStart, softPeek(elementStack), checkMe);
        String qualifiedName = elementMetadata.getQualifiedName();

        // If this is the root element ...
        if (state == State.IN_PROLOG) writeDocType(name);

        if (elementStack.size() > 0) writeIndent();

        if (inCommentedStart) {
            write("<!--");
        } else {
            write('<');
        }
        write(qualifiedName);

        elementStack.push(elementMetadata);
        
        state = State.IN_START_TAG;

        return this;
    }

    /**
     * Closes the start tag, with &gt; or /&gt;, that had been kept open
     * waiting for more namespace declarations and attributes.
     */
    private void terminateStart() {
        verifyOutstandingNamespacePrefixes();
        if (state != State.IN_START_TAG) return;
        writeSchemaLocations();
        write('>');
        attrOnNewLine = false; // reset
        state = State.IN_ELEMENT;
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
        if (state == State.IN_PROLOG || state == State.AFTER_ROOT) {
            badState("text");
        }

        hasContent = true;
        hasIndentedContent = newLine;
        terminateStart();

        if (text != null && text.length() > 0) {
            if (newLine) writeIndent();
            if (escape) text = XMLUtil.escape(text);
            write(text);
        } else if (newLine) {
            write(lineSeparator);
        }

        return this;
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
        escape = false;
        attr(prefix, name, value, newLine);
        escape = true;
        return this;
    }

    /**
     * Same as the text method, but special characters in the value
     * aren't escaped.  This allows entity references to be embedded.
     * @see #text(String)
     * @param text the text
     * @return the calling object to support chaining
     */
    public ElementWAX unescapedText(String text) {
        return unescapedText(text, false);
    }

    /**
     * Same as the text method, but special characters in the value
     * aren't escaped.  This allows entity references to be embedded.
     * @see #text(String, boolean)
     * @param text the text
     * @param newLine true to output the text on a new line; false otherwise
     * @return the calling object to support chaining
     */
    public ElementWAX unescapedText(String text, boolean newLine) {
        escape = false;
        text(text, newLine);
        escape = true;
        return this;
    }

    private void verifyOutstandingNamespacePrefixes() {
        if (checkMe && elementStack.size() > 0) {
            ElementMetadata currentElementMetadata = elementStack.peek();
            currentElementMetadata.verifyOutstandingNamespacePrefixes();
        }
    }

    /**
     * Determines whether XML should be indented.
     * @return true if XML should be indented; false otherwise.
     */
    private boolean willIndent() {
        return indent != null;
    }

    /**
     * Writes a character value to the stream.
     * @param chr the character to write
     */
    private void write(char chr) {
        write(String.valueOf(chr));
    }

    /**
     * Writes a string value to the stream.
     *
     * @param str the String to write
     * @throws IllegalStateException
     *             if attempting to write additional XML data after the output
     *             stream has been closed.
     * @throws WAXIOException if an I/O error occurs.
     */
    private void write(String str) {
        if (writer == null) {
            throw new IllegalStateException(
                "attempting to write XML after close has been called");
        }

        try {
            writer.write(str);
            outputStarted = true;
        } catch (IOException ioException) {
            throw new WAXIOException(ioException);
        }
    }

    /**
     * Writes a DOCTYPE.
     * @param rootElementName the root element name
     */
    private void writeDocType(String rootElementName) {
        if (doctypeSystemId == null && entityDefs.isEmpty()) return;

        write("<!DOCTYPE " + rootElementName);
        if (doctypePublicId != null) {
            write(" PUBLIC \"" + doctypePublicId + "\" \"" +
                doctypeSystemId + '"');
        } else if (doctypeSystemId != null) {
            write(" SYSTEM \"" + doctypeSystemId + '"');
        }

        if (!entityDefs.isEmpty()) {
            write(" [");

            for (String entityDef : entityDefs) {
                if (willIndent()) write(lineSeparator + indent);
                write("<!ENTITY " + entityDef + '>');
            }

            if (willIndent()) write(lineSeparator);
            write(']');

            entityDefs.clear();
        }

        write('>');
        if (willIndent()) write(lineSeparator);
    }

    /**
     * Writes the proper amount of indentation
     * given the current nesting of elements.
     */
    private void writeIndent() {
        if (!willIndent()) return;

        write(lineSeparator);
        int size = elementStack.size();
        for (int i = 0; i < size; ++i) write(indent);
    }

    /**
     * Writes the namespace declaration for the XMLSchema-instance namespace
     * and writes the schemaLocation attribute
     * which associates namespace URIs with schema locations.
     */
    private void writeSchemaLocations() {
        if (namespaceURIToSchemaPathMap.isEmpty()) return;

        // Write the attributes needed to associate XML Schemas
        // with this XML.
        StringBuilder schemaLocation = new StringBuilder();
        for (String uri : namespaceURIToSchemaPathMap.keySet()) {
            String path = namespaceURIToSchemaPathMap.get(uri);
            
            // If not the first pair output ...
            if (schemaLocation.length() > 0) {
                if (willIndent()) {
                    schemaLocation.append(lineSeparator);
                    int size = elementStack.size();
                    for (int i = 0; i <= size; ++i) {
                        schemaLocation.append(indent);
                    }
                } else {
                    schemaLocation.append(' ');
                }
            }
            
            schemaLocation.append(uri + ' ' + path);
        }
        
        namespace("xsi", XMLUtil.XMLSCHEMA_INSTANCE_NS);
        attr("xsi", "schemaLocation", schemaLocation, willIndent());
        attrOnNewLine = true; // for the next attribute
        namespaceURIToSchemaPathMap.clear();
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
        
        if (version == Version.UNSPECIFIED) return;

        String versionString = version.getVersionNumberString();

        // We could also consider using the value of
        // the "file.encoding" system property.
        // However, if we did that then users would have to remember to
        // set that property back to the same value when reading the XML later.
        // Also, many uses might expect WAX to use UTF-8 encoding
        // regardless of the value of that property.

        String encoding = XMLUtil.DEFAULT_ENCODING;
        if (writer instanceof OutputStreamWriter) {
            encoding = ((OutputStreamWriter) writer).getEncoding();
        }

        write("<?xml version=\"" + versionString +
            "\" encoding=\"" + encoding + "\"?>" + lineSeparator);
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
        if (checkMe) XMLUtil.verifyURI(filePath);

        xsltSpecified = true;
        return processingInstruction("xml-stylesheet",
            "type=\"text/xsl\" href=\"" + filePath + "\"");
    }
}