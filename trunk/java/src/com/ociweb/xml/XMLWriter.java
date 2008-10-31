package com.ociweb.xml;

import java.io.*;
import java.util.List;

/**
 * Implementation class used by the <code>WAX</code> class to write formatted
 * XML to a Java <code>Writer</code> instance.
 *
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
 * @author Jeff Grigg
 */
/* package */ class XMLWriter {

    private static final int MAX_INDENT_IN_SPACES = 4;

    private final Writer writer;

    private String lineSeparator;
    private String indent = "  ";
    private int indentionLevel = 0;

    private boolean attrOnNewLine;
    private boolean closeStream = true;
    private boolean hasContent;
    private boolean hasIndentedContent;
    private boolean isClosed;
    private boolean outputStarted;
    private boolean spaceInEmptyElements;
    private boolean verifyUsage;

    public XMLWriter(final Writer writer, final boolean verifyUsage) {
        this.writer = writer;
        this.verifyUsage = verifyUsage;
        this.lineSeparator = System.getProperty("line.separator");
    }

    /**
     * @param count
     * @param stringToCopy
     * @return a <code>String</code> which is <code>count</code> copies of
     *         <code>stringToCopy</code> concatinated together.
     */
    private static String nCopies(final int count, final String stringToCopy) {

        final int capacity = count * stringToCopy.length();

        final StringBuilder sb = new StringBuilder(capacity);
        for (int idx = 0; idx < count; ++idx) {
            sb.append(stringToCopy);
        }

        return sb.toString();
    }

    /**
     * Closes the Writer that is being used to output XML, and insures that
     * nothing else can be written.
     * 
     * @throws WAXIOException
     *             if an I/O error occurs.
     */
    public void close() {
        try {
            if (closeStream) {
                writer.close();
            } else {
                writer.flush();
            }
        } catch (IOException ioException) {
            throw new WAXIOException(ioException);
        }

        isClosed = true;
    }

    /**
     * Calling this method prevents this class from closing the
     * <code>Writer</code> (and any <code>OutputStream</code> it may
     * encapsulate) when <code>close()</code> is called.
     */
    public void doNotCloseOutputStream() {
        closeStream = false;
    }

    /**
     * @return an indention <code>String</code> for the current level of
     *         indention.
     */
    private String getFullIndent() {
        return nCopies(indentionLevel, indent);
    }

    /**
     * @return an indention <code>String</code> for the child of the current
     *         level of indention. (IE: Returns the indention
     *         <code>String</code> for one more than the current level of
     *         indention.
     */
    private String getFullIndentForChild() {
        return nCopies(indentionLevel + 1, indent);
    }

    /**
     * @return a <code>String</code> representing <b>one</b> level of indention.
     */
    public String getIndent() {
        return indent;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * @return approriate white space string for the child level of the current
     *         level. When indention is enabled, this will be a newline sequence
     *         followed by one more indent than the current indention level.
     *         When indention is disabled, this will return only a single space
     *         character.
     */
    public String getWhiteSpaceBreakForChildLevel() {
        if (isIndentDefined()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(lineSeparator);
            sb.append(getFullIndentForChild());
            return sb.toString();
        } else {
            return " ";
        }
    }

    public boolean isAttrOnNewLine() {
        return attrOnNewLine;
    }

    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Determines whether XML should be indented.
     * 
     * @return true if XML should be indented; false otherwise.
     */
    public boolean isIndentDefined() {
        return (indent != null);
    }

    /**
     * Indicates whether a space is added before the slash in empty elements.
     * 
     * @see #setSpaceInEmptyElements(boolean)
     * @return true if a space is added; false otherwise
     */
    public boolean isSpaceInEmptyElements() {
        return spaceInEmptyElements;
    }

    public void resetContentFlags() {
        hasContent = hasIndentedContent = false;
    }

    public void setIndent(final int numSpaces) {
        if (numSpaces < 0) {
            throw new IllegalArgumentException(
                "can't indent a negative number of spaces");
        }

        if (verifyUsage && numSpaces > MAX_INDENT_IN_SPACES) {
            throw new IllegalArgumentException(numSpaces
                + " is an unreasonable indentation");
        }

        indent = "";
        for (int i = 0; i < numSpaces; i++)
            indent += ' ';
    }

    /**
     * Set the string of characters that will be used to indent each level of
     * XML. Only the following values are allowed:
     * <ul>
     * <li><code>null</code> = disable formatting; no newlines or indents are
     * done.</li>
     * <li><code>""</code> <i>(the empty string)</i> = prevents indention, but
     * does newline formatting to make XML more readable</li>
     * <li><code>"\t"</code> <i>(a single tab character)</i> = indent one tab
     * character for each level</li>
     * <li>one to four spaces = indent this number of spaces for each level</li>
     * </ul>
     * All other values throw <code>IllegalArgumentException</code>.
     * 
     * @param indent
     * @throws IllegalArgumentException
     */
    public void setIndent(final String indent) {
        if (verifyUsage) {
            boolean valid = indent == null || indent.length() == 0
                || "\t".equals(indent);

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

            if (!valid
                || (indent != null && indent.length() > MAX_INDENT_IN_SPACES)) {
                throw new IllegalArgumentException("invalid indent value");
            }
        }

        this.indent = indent;
    }

    public void setLineSeparator(final String lineSeparator) {
        if (outputStarted) {
            throw new IllegalStateException(
                "can't change CR characters after output has started");
        }

        if (verifyUsage) {
            boolean valid = WAX.MAC_LINE_SEPARATOR.equals(lineSeparator)
                || WAX.UNIX_LINE_SEPARATOR.equals(lineSeparator)
                || WAX.WINDOWS_LINE_SEPARATOR.equals(lineSeparator);
            if (!valid) {
                throw new IllegalArgumentException(
                    "invalid line separator characters");
            }
        }

        this.lineSeparator = lineSeparator;
    }

    /**
     * Sets whether a space will be added before the closing slash in empty
     * elements. When set true, output will look like "<tag />". When set false,
     * output will look like "<tag/>".
     * 
     * @see #isSpaceInEmptyElements()
     * @param spaceInEmptyElements
     *            true to include a space; false otherwise
     */
    public void setSpaceInEmptyElements(boolean spaceInEmptyElements) {
        this.spaceInEmptyElements = spaceInEmptyElements;
    }

    public void setTrustMe(final boolean trustMe) {
        this.verifyUsage = !trustMe;
    }

    /**
     * Writes a character value to the stream.
     * 
     * @param chr
     *            the character to write
     */
    public void write(char chr) {
        write(String.valueOf(chr));
    }

    /**
     * Writes a string value to the stream.
     * 
     * @param text
     *            the String to write
     * @throws IllegalStateException
     *             if attempting to write additional XML data after the output
     *             stream has been closed.
     * @throws WAXIOException
     *             if an I/O error occurs.
     */
    public void write(final String text) {
        if (isClosed) {
            throw new IllegalStateException(
                "attempting to write XML after close has been called");
        }

        try {
            writer.write(text);
            outputStarted = true;
        } catch (final IOException ioException) {
            throw new WAXIOException(ioException);
        }
    }

    public void writeAttributeEqualsValue(final String qualifiedName,
        final Object value, final boolean newLine, final boolean escape) {
        if (newLine) {
            writeLineBreakAndFullIndent();
        } else {
            write(' ');
        }

        writeNameEqualsValue(qualifiedName, value, escape);
    }

    public void writeComment(final String text, final boolean newLine) {
        if (indentionLevel > 0)
            writeLineBreakAndFullIndent();

        if (newLine && isIndentDefined()) {
            write("<!--");
            writeLineBreakAndFullIndentInChild();
            write(text);
            writeLineBreakAndFullIndent();
            write("-->");
        } else {
            write("<!-- ");
            write(text);
            write(" -->");
        }

        if (indentionLevel == 0 && isIndentDefined())
            writeln();

        hasContent = hasIndentedContent = true;
    }

    public void writeDocType(final String rootElementName,
        final String doctypePublicId, final String doctypeSystemId,
        final List<String> entityDefs) {
        if (doctypeSystemId == null && entityDefs.isEmpty())
            return;

        write("<!DOCTYPE " + rootElementName);
        if (doctypePublicId != null) {
            write(" PUBLIC \"" + doctypePublicId + "\" \"" + doctypeSystemId
                + '"');
        } else if (doctypeSystemId != null) {
            write(" SYSTEM \"" + doctypeSystemId + '"');
        }

        if (!entityDefs.isEmpty()) {
            write(" [");

            for (final String entityDef : entityDefs) {
                writeLineBreakAndFullIndentInChild();
                write("<!ENTITY " + entityDef + '>');
            }

            if (isIndentDefined())
                writeln();
            write(']');
        }

        write('>');
        if (isIndentDefined())
            writeln();
    }

    public void writeEndTag(final String qualifiedName,
        final boolean isCommentElement, final boolean verbose) {
        --indentionLevel;

        if (verbose)
            write('>');

        if (hasContent || verbose) {
            if (hasIndentedContent)
                writeLineBreakAndFullIndent();
            write("</");
            write(qualifiedName);
        } else {
            if (spaceInEmptyElements)
                write(' ');
            write('/');
        }

        write(isCommentElement ? "-->" : ">");

        hasContent = hasIndentedContent = true; // new setting for parent
    }

    /**
     * If indention is being done, then write a line separator character and
     * then write the appropriate indention spaces. In other words, write a new
     * line and then the indent. Writes nothing when indention is disabled.
     */
    private void writeLineBreakAndFullIndent() {
        if (isIndentDefined()) {
            writeln();
            write(getFullIndent());
        }
    }

    /**
     * Like <code>writeOptionalLineBreak()</code>, except that it indents one
     * additional level.
     */
    private void writeLineBreakAndFullIndentInChild() {
        if (isIndentDefined()) {
            writeLineBreakAndFullIndent();
            write(getIndent());
        }
    }

    public void writeln() {
        write(lineSeparator);
    }

    public void writeln(final String text) {
        write(text);
        writeln();
    }

    private void writeNameEqualsValue(final String qualifiedName,
        final Object value, final boolean escape) {
        write(qualifiedName);

        write('=');

        write('"');
        if (escape) {
            write(XMLUtil.escape(value));
        } else {
            write(value.toString());
        }
        write('"');
    }

    public void writeNamespaceDeclaration(final String prefix, final String uri) {
        writeWhiteSpaceBreak();

        write("xmlns");
        if (XMLUtil.hasValue(prefix))
            write(':' + prefix);
        write('=');
        write('"');
        write(uri);
        write('"');

        attrOnNewLine = true; // for the next attribute
    }

    public void writeProcessingInstruction(final String target,
        final String data) {
        if (indentionLevel > 0)
            writeLineBreakAndFullIndent();

        write("<?" + target + ' ' + data + "?>");

        if (indentionLevel == 0 && isIndentDefined())
            writeln();

        hasContent = hasIndentedContent = true;
    }

    public void writeStartTagClose() {
        write('>');
        this.attrOnNewLine = false; // reset
    }

    /**
     * Write the opening and name portion of a start tag.
     * 
     * @param qualifiedName
     * @param inCommentedStart
     */
    public void writeStartTagOpen(final String qualifiedName,
        final boolean inCommentedStart) {
        if (indentionLevel > 0)
            writeLineBreakAndFullIndent();

        if (inCommentedStart) {
            write("<!--");
        } else {
            write('<');
        }
        write(qualifiedName);

        ++indentionLevel;
    }

    public void writeText(final String text, final boolean newLine,
        final boolean escape) {
        if (text != null && text.length() > 0) {
            if (newLine)
                writeLineBreakAndFullIndent();

            if (escape)
                write(XMLUtil.escape(text));
            else
                write(text);
        } else if (newLine) {
            writeln();
        }

        hasContent = true;
        hasIndentedContent = newLine;
    }

    /**
     * Write a white space "break" at the current indention level. When
     * indention is disabled, write only a single space.
     */
    private void writeWhiteSpaceBreak() {
        if (isIndentDefined()) {
            writeLineBreakAndFullIndent();
        } else {
            write(' ');
        }
    }

    public void writeXMLDeclaration(final String versionString) {
        // We could also consider using the value of
        // the "file.encoding" system property.
        // However, if we did that then users would have to remember to
        // set that property back to the same value when reading the XML later.
        // Also, many uses might expect WAX to use UTF-8 encoding
        // regardless of the value of that property.

        final String encoding;
        if (writer instanceof OutputStreamWriter) {
            encoding = ((OutputStreamWriter) writer).getEncoding();
        } else {
            encoding = XMLUtil.DEFAULT_ENCODING;
        }

        writeln("<?xml version=\"" + versionString + "\" encoding=\""
            + encoding + "\"?>");
    }

}
