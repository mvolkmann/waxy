package com.ociweb.xml;

import java.io.*;
import java.util.*;

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

    public static final String NONWINDOWS_CR = "\n";
    public static final String WINDOWS_CR = "\r\n";

    /**
     * The current state of XML output is used to verify that methods
     * in this class aren't called in an illogical order.
     * If they are, an IllegalStateException is thrown.
     */
    private enum State {
        // EMMA flags this, perhaps because it thinks that
        // not all the values are used, but they are.
        IN_PROLOG, IN_START_TAG, IN_ELEMENT, AFTER_ROOT
    }

    private List<String> pendingPrefixes = new ArrayList<String>();

    // Using a TreeMap so keys are kept in sorted order.
    private Map<String, String> namespaceURIToSchemaPathMap =
        new TreeMap<String, String>();

    private List<String> entityDefs = new ArrayList<String>();
    private Stack<String> parentStack = new Stack<String>();
    private Stack<String> prefixesStack = new Stack<String>();
    private State state = State.IN_PROLOG;
    private String cr;
    private String doctypePublicId;
    private String doctypeSystemId;
    private String encoding = XMLUtil.DEFAULT_ENCODING;
    private String indent = "  ";
    private Writer writer;
    private boolean attrOnNewLine;
    private boolean checkMe = true;
    private boolean closeStream = true;
    private boolean defaultNSOnCurrentElement;
    private boolean dtdSpecified;
    private boolean escape = true;
    private boolean hasContent;
    private boolean hasIndentedContent;
    private boolean inCommentedStart;
    private boolean outputStarted;
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
     * @param filePath the file path
     */
    public WAX(String filePath) { this(filePath, Version.UNSPECIFIED); }
    public WAX(String filePath, Version version) {
        this(makeWriter(filePath), version);
    }

    /**
     * Creates a WAX that writes to a given Writer.
     * The writer will be closed by the close method of this class.
     * 
     * @param writer the Writer
     */
    public WAX(Writer writer) { this(writer, Version.UNSPECIFIED); }
    public WAX(Writer writer, Version version) {
        this.writer = writer;
        useNonWindowsCR();

        if (writer instanceof OutputStreamWriter) {
            encoding = ((OutputStreamWriter) writer).getEncoding();
        }

        // We could also consider using the value of
        // the "file.encoding" system property.
        // However, if we did that then users would have to remember to
        // set that property back to the same value when reading the XML later.
        // Also, many uses might expect WAX to use UTF-8 encoding
        // regardless of the value of that property.

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
     * @param prefix the namespace prefix for the attribute
     * @param name the attribute name
     * @param value the attribute value
     * @param newLine true to write on a new line; false otherwise
     * @return the calling object to support chaining
     */
    public StartTagWAX attr(
        String prefix, String name, Object value, boolean newLine) {

        if (state != State.IN_START_TAG) badState("attr");

        boolean hasPrefix = prefix != null && prefix.length() > 0;

        if (checkMe) {
            if (hasPrefix) {
                XMLUtil.verifyName(prefix);
                pendingPrefixes.add(prefix);
            }
            XMLUtil.verifyName(name);
        }

        String qName = hasPrefix ? prefix + ':' + name : name;

        if (newLine) {
            writeIndent();
        } else {
            write(' ');
        }

        if (escape) value = XMLUtil.escape(value);
        
        write(qName + "=\"" + value + "\"");

        return this;
    }

    /**
     * Throws an IllegalStateException that indicates
     * the method that was called and the current state that was invalid.
     * @param methodName the method name
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
     * @param text the text
     * @param newLine true to output the text on a new line; false otherwise
     * @return the calling object to support chaining
     */
    public ElementWAX cdata(String text, boolean newLine) {
        if (state == State.IN_PROLOG || state == State.AFTER_ROOT) {
            badState("cdata");
        }

        escape = false;
        text("<![CDATA[" + text + "]]>", newLine);
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
     * @param prefix the namespace prefix of the child element
     * @param name the child element name
     * @param text the child element text content
     * @return the calling object to support chaining
     */
    public ElementWAX child(String prefix, String name, String text) {
        if (state == State.AFTER_ROOT) badState("child");
        return start(prefix, name).text(text).end();
    }

    /**
     * Terminates all unterminated elements,
     * closes the Writer that is being used to output XML,
     * and insures that nothing else can be written.
     */
    public void close() {
        if (writer == null) throw new IllegalStateException("already closed");

        // Verify that a root element has been written.
        if (state == State.IN_PROLOG) badState("close");

        // End all the unended elements.
        while (parentStack.size() > 0) end();

        try {
            if (closeStream) {
                writer.close();
            } else {
                writer.flush();
            }
        } catch (IOException e) {
            // EMMA flags this as uncovered, but I can't make this happen.
            throw new RuntimeException(e);
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
     * @param text the comment text (cannot contain "--")
     * @param newLine true to output the text on a new line; false otherwise
     * @return the calling object to support chaining
     */
    public PrologOrElementWAX comment(String text, boolean newLine) {
        // Comments can be output in any state.

        if (checkMe) XMLUtil.verifyComment(text);
        
        hasContent = hasIndentedContent = true;
        terminateStart();
        if (parentStack.size() > 0) writeIndent();

        if (newLine) {
            write("<!--");
            writeIndent();
            write(indent);
            write(text);
            writeIndent();
            write("-->");
        } else {
            write("<!-- " + text + " -->");
        }

        if (willIndent() && parentStack.size() == 0) write(cr);

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
     * @param publicId the public ID of the DTD
     * @param systemId the file path or URL to the DTD
     * @return the calling object to support chaining
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
     * @param verbose true to not consider shorthand way; false to consider it
     * @return the calling object to support chaining
     */
    public ElementWAX end(boolean verbose) {
        if (state == State.IN_PROLOG || state == State.AFTER_ROOT) {
            badState("end");
        }
        if (checkMe) verifyPrefixes();

        writeSchemaLocations();

        String name = parentStack.pop();

        // Namespace prefixes that were in scope for this element
        // are no longer in scope.
        prefixesStack.pop();

        // Check for hypen at beginning of element name
        // which indicates that the commentedStart method was used.
        boolean wasCommentedStart = name.charAt(0) == '-';

        if (hasContent || verbose) {
            if (verbose) write(">");
            if (hasIndentedContent) writeIndent();
            write("</");
            write(wasCommentedStart ? name.substring(1) : name);
            write(wasCommentedStart ? "-->" : ">");
        } else {
            write(wasCommentedStart ? "/-->" : "/>");
        }

        hasContent = hasIndentedContent = true; // new setting for parent

        state = parentStack.size() == 0 ?
            State.AFTER_ROOT : State.IN_ELEMENT;

        return this;
    }

    /**
     * Adds an entity definition to the internal subset of the DOCTYPE.
     * @param name
     * @param value
     * @return the calling object to support chaining
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
     * Gets the carriage return characters currently being used.
     * @return the carriage return characters
     */
    public String getCR() {
        return cr;
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
     * Determines whether a given namespace prefix is currently in scope.
     * @param prefix the namespace prefix
     * @return true if it is in scope; false otherwise
     */
    private boolean isInScopePrefix(String prefix) {
        for (String prefixes : prefixesStack) {
            if (prefixes == null) continue;

            // Check for the special case where we are testing for the
            // default namespace and that's the only namespace in scope.
            if (prefix.length() == 0 && prefixes.length() == 0) return true;

            StringTokenizer st = new StringTokenizer(prefixes, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.equals(prefix)) return true;
            }
        }

        return false;
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
     * @param filePath the file path
     * @return the Writer
     */
    private static Writer makeWriter(String filePath) {
        try {
            return new FileWriter(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
     * @param prefix the namespace prefix
     * @param uri the namespace URI
     * @param schemaPath the path to the XML Schema
     * @return the calling object to support chaining
     */
    public StartTagWAX namespace(
        String prefix, String uri, String schemaPath) {

        if (state != State.IN_START_TAG) badState("namespace");

        if (prefix == null) prefix = "";
        boolean hasPrefix = prefix.length() > 0;
        String prefixesOnCurrentElement = prefixesStack.pop();

        if (checkMe) {
            if (hasPrefix) XMLUtil.verifyName(prefix);
            XMLUtil.verifyURI(uri);
            if (schemaPath != null) XMLUtil.verifyURI(schemaPath);

            // Verify that the prefix isn't already defined
            // on the current element.
            if (hasPrefix) {
                if (prefixesOnCurrentElement != null) {
                    StringTokenizer st =
                        new StringTokenizer(prefixesOnCurrentElement, ",");
                    while (st.hasMoreTokens()) {
                        String definedPrefix = st.nextToken();
                        if (prefix.equals(definedPrefix)) {
                            throw new IllegalArgumentException(
                                "The namespace prefix \"" + prefix +
                                "\" is already defined on the current element.");
                        }
                    }
                }
            } else if (defaultNSOnCurrentElement) {
                throw new IllegalArgumentException("The default namespace " +
                    "is already defined on the current element.");
            }
        }

        if (willIndent()) {
            writeIndent();
        } else {
            write(' ');
        }
        
        write("xmlns");
        if (hasPrefix) write(':' + prefix);
        write("=\"" + uri + "\"");
        
        if (schemaPath != null) {
            namespaceURIToSchemaPathMap.put(uri, schemaPath);
        }

        if (hasPrefix) {
            // Add this prefix to the list of those in scope for this element.
            if (prefixesOnCurrentElement == null) {
                prefixesOnCurrentElement = prefix;
            } else {
                prefixesOnCurrentElement += ',' + prefix;
            }
        } else {
            defaultNSOnCurrentElement = true;
        }

        // Note that the entry for the current element was popped off
        // near the beginning of this method, so we're pushing it back on.
        prefixesStack.push(prefixesOnCurrentElement);

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
     * @param target the processing instruction target
     * @param data the processing instruction data
     * @return the calling object to support chaining
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
        if (parentStack.size() > 0) writeIndent();

        write("<?" + target + ' ' + data + "?>");
        if (willIndent() && parentStack.size() == 0) write(cr);

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
    public StartTagWAX rawAttr(String name, Object value) {
        return rawAttr("", name, value);
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
    public StartTagWAX rawAttr(String prefix, String name, Object value) {
        return rawAttr(prefix, name, value, false);
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
    public StartTagWAX rawAttr(
        String prefix, String name, Object value, boolean newLine) {
        escape = false;
        attr(prefix, name, value);
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
    public ElementWAX rawText(String text) {
        return rawText(text, false);
    }

    /**
     * Same as the text method, but special characters in the value
     * aren't escaped.  This allows entity references to be embedded.
     * @see #text(String, boolean)
     * @param text the text
     * @param newLine true to output the text on a new line; false otherwise
     * @return the calling object to support chaining
     */
    public ElementWAX rawText(String text, boolean newLine) {
        escape = false;
        text(text, newLine);
        escape = true;
        return this;
    }

    /**
     * Sets the indentation characters to use.
     * Unless "trust me" is set to true, the only valid values are
     * a single tab, one or more spaces, an empty string, or null.
     * Passing "" causes elements to be output on separate lines,
     * but not indented.
     * Passing null causes all output to be on a single line.
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
     * @param numSpaces the number of spaces
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
     * Don't output indent output or write carriage returns.
     * Write out the XML on a single line.
     */
    public void noIndentsOrCRs() {
        setIndent(null);
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
     * Writes the start tag for a given element name, but doesn't terminate it.
     * @param name the element name
     * @return the calling object to support chaining
     */
    public StartTagWAX start(String name) {
        return start(null, name);
    }

    /**
     * Writes the start tag for a given element name, but doesn't terminate it.
     * @param prefix the namespace prefix to used on the element
     * @param name the element name
     * @return the calling object to support chaining
     */
    public StartTagWAX start(String prefix, String name) {
        hasContent = hasIndentedContent = true;
        terminateStart();
        hasContent = hasIndentedContent = false;

        if (state == State.AFTER_ROOT) badState("start");

        boolean hasPrefix = prefix != null && prefix.length() > 0;
        if (checkMe) {
            if (hasPrefix) {
                XMLUtil.verifyName(prefix);
                pendingPrefixes.add(prefix);
            }
            XMLUtil.verifyName(name);
        }

        // If this is the root element ...
        if (state == State.IN_PROLOG) writeDocType(name);

        // Can't add to pendingPrefixes until
        // previous start tag has been terminated.
        if (checkMe && hasPrefix) {
            pendingPrefixes.add(prefix);
        }

        if (parentStack.size() > 0) writeIndent();

        String qName = hasPrefix ? prefix + ':' + name : name;

        if (inCommentedStart) {
            write("<!--" + qName);
            // Add a "marker" to the element name on the stack
            // so the end method knows to terminate the comment.
            parentStack.push('-' + qName);
        } else {
            write('<' + qName);
            parentStack.push(qName);
        }

        defaultNSOnCurrentElement = false;
        
        // No namespace prefixes have been associated with this element yet.
        prefixesStack.push(null);

        state = State.IN_START_TAG;

        return this;
    }

    /**
     * Closes the start tag, with &gt; or /&gt;, that had been kept open
     * waiting for more namespace declarations and attributes.
     */
    private void terminateStart() {
        if (checkMe) verifyPrefixes();
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
     * @param text the text
     * @param newLine true to output the text on a new line; false otherwise
     * @return the calling object to support chaining
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
            write(cr);
        }

        return this;
    }

    /**
     * Uses \n for carriage returns which is appropriate
     * on every platform except Windows.
     * This is the default.
     */
    public void useNonWindowsCR() {
        if (outputStarted) {
            throw new IllegalStateException(
                "can't change CR characters after output has started");
        }
        cr = NONWINDOWS_CR;
    }

    /**
     * Uses \r\n for carriage returns which is appropriate
     * only on the Windows platform.
     * This is not the default.
     */
    public void useWindowsCR() {
        if (outputStarted) {
            throw new IllegalStateException(
                "can't change CR characters after output has started");
        }
        cr = WINDOWS_CR;
    }

    /**
     * Verifies that all the pending namespace prefix are currently in scope.
     * @throws IllegalArgumentException if any aren't in scope
     */
    private void verifyPrefixes() {
        for (String prefix : pendingPrefixes) {
            if (!isInScopePrefix(prefix)) {
                throw new IllegalArgumentException(
                    "The namespace prefix \"" + prefix + "\" isn't in scope.");
            }
        }

        pendingPrefixes.clear();
    }

    /**
     * Determines whether XML should be indented.
     * @return true if XML should be indented; false otherwise.
     */
    private boolean willIndent() {
        return indent != null;
    }

    /**
     * Writes the toString value of an Object to the stream.
     * @param obj the Object
     */
    private void write(Object obj) {
        if (writer == null) {
            // EMMA flags this as uncovered, but I can't make this happen.
            throw new IllegalStateException(
                "attempting to write XML after close has been called");
        }

        try {
            writer.write(obj.toString());
            outputStarted = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                if (willIndent()) write(cr + indent);
                write("<!ENTITY " + entityDef + '>');
            }

            if (willIndent()) write(cr);
            write(']');

            entityDefs.clear();
        }

        write('>');
        if (willIndent()) write(cr);
    }

    /**
     * Writes the proper amount of indentation
     * given the current nesting of elements.
     */
    private void writeIndent() {
        if (!willIndent()) return;

        write(cr);
        int size = parentStack.size();
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
        String schemaLocation = "";
        for (String uri : namespaceURIToSchemaPathMap.keySet()) {
            String path = namespaceURIToSchemaPathMap.get(uri);
            
            // If not the first pair output ...
            if (schemaLocation.length() > 0) {
                if (willIndent()) {
                    schemaLocation += cr;
                    int size = parentStack.size();
                    for (int i = 0; i <= size; ++i) {
                        schemaLocation += indent;
                    }
                } else {
                    schemaLocation += ' ';
                }
            }
            
            schemaLocation += uri + ' ' + path;
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
     * @param version the XML version
     */
    private void writeXMLDeclaration(Version version) {
        if (version == Version.UNSPECIFIED) return;

        String versionString =
            version == Version.V1_0 ? "1.0" :
            version == Version.V1_1 ? "1.1" :
            version == Version.V1_2 ? "1.2" :
                null; // should never happen

        if (versionString == null) {
            throw new IllegalArgumentException("unsupported XML version");
        }

        write("<?xml version=\"" + versionString +
            "\" encoding=\"" + encoding + "\"?>" + cr);
    }

    /**
     * Writes an "xml-stylesheet" processing instruction.
     * @param filePath the path to the XSLT stylesheet
     * @return the calling object to support chaining
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