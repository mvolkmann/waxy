using System;
using System.IO;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Text;

namespace WAXNamespace
{
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
    public class WAX : PrologOrElementWAX, StartTagWAX {

        /**
         * The current state of XML output is used to verify that methods
         * in this class aren't called in an illogical order.
         * If they are, an InvalidOperationException is thrown.
         */
        private enum State {
            // EMMA flags this, perhaps because it thinks that
            // not all the values are used, but they are.
            IN_PROLOG, IN_START_TAG, IN_ELEMENT, AFTER_ROOT
        }

        public enum Version { UNSPECIFIED, V1_0, V1_1, V1_2 };

        private IList pendingPrefixes = new ArrayList(); // ArrayList<string>

        // Keys are kept in sorted order.
        private IDictionary<string, string> namespaceURIToSchemaPathMap = new SortedList<string, string>();

        private IList entityDefs = new ArrayList(); // ArrayList<string>
        private Stack<string> parentStack = new Stack<string>();
        private Stack<string> prefixesStack = new Stack<string>();
        private State state = State.IN_PROLOG;
        private string dtdFilePath;
        private string indent = "  ";
        private TextWriter writer;
        private bool attrOnNewLine;
        private bool checkMe = true;
        private bool closeStream = true;
        private bool hasContent;
        private bool hasIndentedContent;

        /**
         * Creates a WAX that writes to stdout.
         */
        public WAX()
            : this(Version.UNSPECIFIED)
        {
        }

        public WAX(Version version)
            : this(Console.Out, version)
        {
            closeStream = false;
        }

        /**
         * Creates a WAX that writes to a given Stream.
         * The stream will be closed by the close method of this class.
         * @param stream the Stream
         */
        public WAX(Stream stream)
            : this(stream, Version.UNSPECIFIED)
        {
        }

        public WAX(Stream stream, Version version)
            : this(MakeWriter(stream), version)
        {
        }

        /**
         * Creates a WAX that writes to a given file path.
         * @param filePath the file path
         */
        public WAX(string filePath)
            : this(filePath, Version.UNSPECIFIED)
        {
        }

        public WAX(string filePath, Version version)
            : this(MakeWriter(filePath), version)
        {
        }

        /**
         * Creates a WAX that writes to a given TextWriter.
         * The writer will be closed by the close method of this class.
         * 
         * @param writer the TextWriter
         */
        public WAX(TextWriter writer)
            : this(writer, Version.UNSPECIFIED)
        {
        }

        public WAX(TextWriter writer, Version version) {
            this.writer = writer;
            WriteXMLDeclaration(version);
        }

        /**
         * Writes an attribute for the currently open element start tag.
         * @param name the attribute name
         * @param value the attribute value
         * @return the calling object to support chaining
         */
        public StartTagWAX Attr(string name, Object value) {
            return Attr(null, name, value);
        }

        /**
         * Writes an attribute for the currently open element start tag.
         * @param prefix the namespace prefix for the attribute
         * @param name the attribute name
         * @param value the attribute value
         * @return the calling object to support chaining
         */
        public StartTagWAX Attr(string prefix, string name, Object value) {
            return Attr(attrOnNewLine, prefix, name, value);
        }

        /**
         * Writes an attribute for the currently open element start tag.
         * @param newLine true to write on a new line; false otherwise
         * @param prefix the namespace prefix for the attribute
         * @param name the attribute name
         * @param value the attribute value
         * @return the calling object to support chaining
         */
        public StartTagWAX Attr(
            bool newLine, string prefix, string name, Object value) {

            if (checkMe) {
                if (state != State.IN_START_TAG) {
                    // EMMA incorrectly says this isn't called.
                    BadState("attr");
                }

                if (prefix != null) {
                    XMLUtil.VerifyNMToken(prefix);
                    pendingPrefixes.Add(prefix);
                }

                XMLUtil.VerifyNMToken(name);
            }

            bool hasPrefix = prefix != null && prefix.Length > 0;
            string qName = hasPrefix ? prefix + ':' + name : name;

            if (newLine) {
                WriteIndent();
            } else {
                Write(' ');
            }
            
            Write(qName + "=\"" + value + "\"");

            return this;
        }

        /**
         * Throws an InvalidOperationException that indicates
         * the method that was called and the current state that was invalid.
         * @param methodName the method name
         */
        private void BadState(string methodName) {
            throw new InvalidOperationException(
                "can't call " + methodName + " when state is " + state);
        }

        /**
         * Writes a blank line to increase readability of the XML.
         * @return the calling object to support chaining
         */
        public ElementWAX BlankLine() {
            return NlText("");
        }

        /**
         * Writes a CDATA section in the content of the current element.
         * @param text the text
         * @return the calling object to support chaining
         */
        public ElementWAX CData(string text) {
            if (checkMe) {
                if (state == State.IN_PROLOG ||
                    state == State.AFTER_ROOT) {
                    // EMMA incorrectly says this isn't called.
                    BadState("cdata");
                }
            }

            return Text("<![CDATA[" + text + "]]>", true, false);
        }
        
        /**
         * A convenience method that is a shortcut for
         * Start(name).Text(text).End().
         * @param name the child element name
         * @param text the child element text content
         * @return the calling object to support chaining
         */
        public ElementWAX Child(string name, string text) {
            return Child(null, name, text);
        }

        /**
         * A convenience method that is a shortcut for
         * Start(prefix, name).Text(text).End().
         * @param prefix the namespace prefix of the child element
         * @param name the child element name
         * @param text the child element text content
         * @return the calling object to support chaining
         */
        public ElementWAX Child(string prefix, string name, string text) {
            if (checkMe && state == State.AFTER_ROOT) {
                // EMMA incorrectly says this isn't called.
                BadState("child");
            }

            return Start(prefix, name).Text(text).End();
        }

        /**
         * Terminates all unterminated elements,
         * closes the TextWriter that is being used to output XML,
         * and insures that nothing else can be written.
         */
        public void Close() {
            if (writer == null) throw new InvalidOperationException("already closed");

            // Verify that a root element has been written.
            // EMMA incorrectly says this isn't called.
            if (checkMe && state == State.IN_PROLOG) BadState("close");

            // End all the unended elements.
            while (parentStack.Count > 0) End();

            try {
                if (closeStream) {
                    writer.Close();
                } else {
                    writer.Flush();
                }
            } catch (IOException e) {
                // EMMA flags this as uncovered, but I can't make this happen.
                throw new Exception("Unexpected IOException", e);
            }

            writer = null;
        }

        /**
         * Writes a comment (&lt;!-- text --&gt;).
         * @param text the comment text (cannot contain "--")
         * @return the calling object to support chaining
         */
        public PrologOrElementWAX Comment(string text) {
            // Comments can be output in any state.

            if (checkMe) XMLUtil.VerifyComment(text);
            
            hasContent = hasIndentedContent = true;
            TerminateStart();
            if (parentStack.Count > 0) WriteIndent();

            Write("<!-- " + text + " -->");
            if (WillIndent() && parentStack.Count == 0) Write('\n');

            return this;
        }

        /**
         * Writes a DOCTYPE that associates a DTD with the XML document.
         * @param filePath the path to the DTD
         * @return the calling object to support chaining
         */
        public PrologWAX Dtd(string filePath) {
            if (checkMe) {
                if (state != State.IN_PROLOG) BadState("dtd");
                XMLUtil.VerifyURI(filePath);
            }

            dtdFilePath = filePath;
            return this;
        }

        /**
         * Terminates the current element.
         * It does so in the shorthand way (/&gt;) if the element has no content,
         * and in the long way (&lt;/name&gt;) if it does.
         * @return the calling object to support chaining
         */
        public ElementWAX End() {
            if (checkMe) {
                if (state == State.IN_PROLOG || state == State.AFTER_ROOT) {
                    // EMMA incorrectly says this isn't called.
                    BadState("end");
                }

                VerifyPrefixes();
            }

            WriteSchemaLocations();

            string name = parentStack.Pop();

            // Namespace prefixes that were in scope for this element
            // are no longer in scope.
            prefixesStack.Pop();

            if (hasContent) {
                if (hasIndentedContent) WriteIndent();
                Write("</" + name + ">");
            } else {
                Write("/>");
            }

            hasContent = hasIndentedContent = true; // new setting for parent

            state = parentStack.Count == 0 ?
                State.AFTER_ROOT : State.IN_ELEMENT;

            return this;
        }

        /**
         * Adds an entity definition to the internal subset of the DOCTYPE.
         * @param name
         * @param value
         * @return the calling object to support chaining
         */
        public PrologWAX EntityDef(string name, string value) {
            if (checkMe && state != State.IN_PROLOG) BadState("entity");
            entityDefs.Add(name + " \"" + value + '"');
            return this;
        }

        /**
         * Adds an external entity definition to the internal subset of the DOCTYPE.
         * @param name the name
         * @param filePath the filePath
         * @return the calling object to support chaining
         */
        public PrologWAX ExternalEntityDef(string name, string filePath) {
            return EntityDef(name + " SYSTEM", filePath);
        }

        // TODO: Make this a property:
        /**
         * Gets the indentation characters being used.
         * @return the indentation characters
         */
        public string GetIndent() {
            return indent;
        }

        /**
         * Determines whether a given namespace prefix is currently in scope.
         * @param prefix the namespace prefix
         * @return true if it is in scope; false otherwise
         */
        private bool IsInScopePrefix(string prefix) {
            foreach (string prefixes in prefixesStack) {
                if (prefixes == null) continue;

                // Check for the special case where we are testing for the
                // default namespace and that's the only namespace in scope.
                if (prefix.Length == 0 && prefixes.Length == 0) return true;

                if (("," + prefixes + ",").IndexOf("," + prefix + ",") >= 0)
                    return true;
                //stringTokenizer st = new stringTokenizer(prefixes, ",");
                //while (st.hasMoreTokens()) {
                //    string token = st.nextToken();
                //    if (token.Equals(prefix)) return true;
                //}
            }

            return false;
        }

        // TODO: Make this a property:
        /**
         * Gets whether "trust me" mode is enabled.
         * @see #setTrustMe
         * @return true if error checking is disabled; false if enabled
         */
        public bool IsTrustMe() {
            return !checkMe;
        }

        /**
         * Creates a TextWriter for a given file path.
         * @param filePath the file path
         * @return the TextWriter
         */
        private static TextWriter MakeWriter(string filePath) {
            try {
                Stream outputStream = File.OpenWrite(filePath);
                return new StreamWriter(outputStream);
            } catch (IOException e) {
                throw new Exception("Unexpected IOException", e);
            }
        }

        /**
         * Creates a TextWriter for a given Stream.
         * @param stream
         * @return the TextWriter
         */
        private static TextWriter MakeWriter(Stream stream)
        {
            try
            {
                return new StreamWriter(stream);
            }
            catch (IOException e)
            {
                throw new Exception("Unexpected IOException", e);
            }
        }

        /**
         * Writes a namespace declaration for the default namespace
         * in the start tag of the current element.
         * @param uri the namespace URI
         * @return the calling object to support chaining
         */
        public StartTagWAX Namespace(string uri) {
            return Namespace(null, uri);
        }

        /**
         * Writes a namespace declaration in the start tag of the current element.
         * To define the default namespace, use the namespace method
         * that takes a single argument.
         * @param prefix the namespace prefix
         * @param uri the namespace URI
         * @return the calling object to support chaining
         */
        public StartTagWAX Namespace(string prefix, string uri) {
            return Namespace(prefix, uri, null);
        }

        /**
         * Writes a namespace declaration in the start tag of the current element.
         * To define the default namespace, use the namespace method
         * that takes a single argument.
         * @param prefix the namespace prefix (null or "" for default namespace)
         * @param uri the namespace URI
         * @param schemaPath the path to the XML Schema
         * @return the calling object to support chaining
         */
        public StartTagWAX Namespace(
            string prefix, string uri, string schemaPath) {

            if (prefix == null) prefix = "";
            bool hasPrefix = prefix.Length > 0;

            if (checkMe) {
                if (state != State.IN_START_TAG) {
                    // EMMA incorrectly says this isn't called.
                    BadState("namespace");
                }

                if (hasPrefix) XMLUtil.VerifyNMToken(prefix);
                XMLUtil.VerifyURI(uri);
//TODO:                if (schemaPath != null) XMLUtil.VerifyURI(schemaPath);
            }

            // Verify that the prefix isn't already defined in the current scope.
            if (IsInScopePrefix(prefix)) {
                throw new ArgumentException(
                    "The namespace prefix \"" + prefix + "\" is already in scope.");
            }

            if (WillIndent()) {
                WriteIndent();
            } else {
                Write(' ');
            }
            
            Write("xmlns");
            if (hasPrefix) Write(':' + prefix);
            Write("=\"" + uri + "\"");
            
            if (schemaPath != null) {
                namespaceURIToSchemaPathMap[uri] = schemaPath;
            }

            // Add this prefix to the list of those in scope for this element.
            string prefixes = prefixesStack.Pop();
            if (prefixes == null) {
                prefixes = prefix;
            } else {
                prefixes += ',' + prefix;
            }
            prefixesStack.Push(prefixes);

            attrOnNewLine = true; // for the next attribute

            return this;
        }

        /**
         * Creates a new WAX object that writes to stdout and
         * returns it as an interface type that restricts the first method call
         * to be one that is valid for the initial ouptut.
         * @return a specific interface that WAX implements.
         */
        public static PrologWAX NewInstance() {
            return new WAX();
        }

        // TODO: Change to match constructors.
        /**
         * Creates a new WAX object that writes to a given OutputStream and
         * returns it as an interface type that restricts the first method call
         * to be one that is valid for the initial ouptut.
         * @param os the OutputStream
         * @return a specific interface that WAX implements.
         */
        //public static PrologWAX NewInstance(OutputStream os) {
        //    return new WAX(os);
        //}

        /**
         * Creates a new WAX object that writes to a given file path and
         * returns it as an interface type that restricts the first method call
         * to be one that is valid for the initial ouptut.
         * @param filePath the file path
         * @return a specific interface that WAX implements.
         */
        public static PrologWAX NewInstance(string filePath) {
            return new WAX(filePath);
        }

        /**
         * Creates a new WAX object that writes to a given TextWriter and
         * returns it as an interface type that restricts the first method call
         * to be one that is valid for the initial ouptut.
         * @param writer the TextWriter
         * @return a specific interface that WAX implements.
         */
        public static PrologWAX NewInstance(TextWriter writer) {
            return new WAX(writer);
        }

        /**
         * Writes text preceded by a newline.
         * @param text the text
         * @return the calling object to support chaining
         */
        public ElementWAX NlText(string text) {
            return Text(text, true, checkMe);
        }

        /**
         * Writes a processing instruction.
         * @param target the processing instruction target
         * @param data the processing instruction data
         * @return the calling object to support chaining
         */
        public PrologOrElementWAX ProcessingInstruction(
            string target, string data) {

            if (checkMe) {
                if (state == State.AFTER_ROOT) {
                    // EMMA incorrectly says this isn't called.
                    BadState("pi");
                }
                XMLUtil.VerifyNMToken(target);
            }
            
            hasContent = hasIndentedContent = true;
            TerminateStart();
            if (parentStack.Count > 0) WriteIndent();

            Write("<?" + target + ' ' + data + "?>");
            if (WillIndent() && parentStack.Count == 0) Write('\n');

            return this;
        }

        // TODO: Make this a property:
        /**
         * Sets the indentation characters to use.
         * The only valid values are
         * a single tab, one or more spaces, an empty string, or null.
         * Passing "" causes elements to be output on separate lines,
         * but not indented.
         * Passing null causes all output to be on a single line.
         */
        public void SetIndent(string indent) {
            bool valid =
                indent == null || indent.Length == 0 || "\t".Equals(indent);
            
            if (!valid) {
                // It can only be valid now if every character is a space.
                valid = true; // assume
                char[] chars = indent.ToCharArray();
                for (int i = 0; i < chars.Length; ++i) {
                    if (chars[i] != ' ') {
                        valid = false;
                        break;
                    }
                }
            }
            
            if (!valid) {
                throw new ArgumentException("invalid indent value");
            }
            
            this.indent = indent;
        }

        /**
         * Sets the number of spaces to use for indentation.
         * The number must be >= 0 and <= 4.
         * @param numSpaces the number of spaces
         */
        public void SetIndent(int numSpaces) {
            if (numSpaces < 0) {
                throw new ArgumentException(
                    "can't indent a negative number of spaces");
            }

            if (numSpaces > 4) {
                throw new ArgumentException(
                    numSpaces + " is an unreasonable indentation");
            }

            indent = "";
            for (int i = 0; i < numSpaces; i++) indent += ' ';
        }

        // TODO: Make this a property:
        /**
         * Gets whether "trust me" mode is enabled.
         * When disabled (the default),
         * proper order of method calls is verified,
         * method parameter values are verified,
         * element and attribute names are verified to be NMTokens,
         * and reserved characters in element/attribute text
         * are replaced by built-in entity references.
         * The main reason to enable "trust me" mode is for performance
         * which is typically good even when disabled.
         * @see #isTrustMe
         * @param trustMe true to disable error checking; false to enable it
         */
        public void SetTrustMe(bool trustMe) {
            this.checkMe = !trustMe;
        }

        /**
         * Writes the start tag for a given element name, but doesn't terminate it.
         * @param name the element name
         * @return the calling object to support chaining
         */
        public StartTagWAX Start(string name) {
            return Start(null, name);
        }

        /**
         * Writes the start tag for a given element name, but doesn't terminate it.
         * @param name the element name
         * @return the calling object to support chaining
         */
        public StartTagWAX Start(string prefix, string name) {
            hasContent = hasIndentedContent = true;
            TerminateStart();
            hasContent = false;

            if (checkMe) {
                if (state == State.AFTER_ROOT) {
                    // EMMA incorrectly says this isn't called.
                    BadState("start");
                }
                if (prefix != null) {
                    XMLUtil.VerifyNMToken(prefix);
                    pendingPrefixes.Add(prefix);
                }
                XMLUtil.VerifyNMToken(name);
            }

            // If this is the root element ...
            if (state == State.IN_PROLOG) WriteDocType(name);

            // Can't add to pendingPrefixes until
            // previous start tag has been terminated.
            if (checkMe && prefix != null) pendingPrefixes.Add(prefix);

            if (parentStack.Count > 0) WriteIndent();

            bool hasPrefix = prefix != null && prefix.Length > 0;
            string qName = hasPrefix ? prefix + ':' + name : name;

            Write('<' + qName);

            parentStack.Push(qName);

            // No namespace prefixes have been associated with this element yet.
            prefixesStack.Push(null);

            state = State.IN_START_TAG;

            return this;
        }

        /**
         * Closes the start tag, with &gt; or /&gt;, that had been kept open
         * waiting for more namespace declarations and attributes.
         */
        private void TerminateStart() {
            if (checkMe) VerifyPrefixes();
            if (state != State.IN_START_TAG) return;
            WriteSchemaLocations();
            Write('>');
            attrOnNewLine = false; // reset
            state = State.IN_ELEMENT;
        }

        /**
         * Writes text inside the content of the current element.
         * @param text the text
         * @return the calling object to support chaining
         */
        public ElementWAX Text(string text) {
            return Text(text, false, checkMe);
        }

        /**
         * Writes text inside the content of the current element.
         * @param text the text
         * @param newLine true to output the text on a new line; false otherwise
         * @param escape true to escape special characters in the text;
         *               false to avoid checking for them
         * @return the calling object to support chaining
         */
        public ElementWAX Text(string text, bool newLine, bool escape) {
            if (checkMe) {
                if (state == State.IN_PROLOG || state == State.AFTER_ROOT) {
                    // EMMA incorrectly says this isn't called.
                    BadState("text");
                }
            }

            hasContent = true;
            hasIndentedContent = newLine;
            TerminateStart();

            if (text != null && text.Length > 0) {
                if (newLine) WriteIndent();
                if (escape) text = XMLUtil.Escape(text);
                Write(text);
            } else if (newLine) {
                Write('\n');
            }

            return this;
        }

        /**
         * Verifies that all the pending namespace prefix are currently in scope.
         * @throws ArgumentException if any aren't in scope
         */
        private void VerifyPrefixes() {
            foreach (string prefix in pendingPrefixes) {
                if (!IsInScopePrefix(prefix)) {
                    throw new ArgumentException(
                        "The namespace prefix \"" + prefix + "\" isn't in scope.");
                }
            }

            pendingPrefixes.Clear();
        }

        /**
         * Determines whether XML should be indented.
         * @return true if XML should be indented; false otherwise.
         */
        private bool WillIndent() {
            return indent != null;
        }

        /**
         * Writes the tostring value of an Object to the stream.
         * @param obj the Object
         */
        private void Write(Object obj) {
            if (writer == null) {
                // EMMA flags this as uncovered, but I can't make this happen.
                throw new InvalidOperationException(
                    "attempting to write XML after close has been called");
            }

            try {
                writer.Write(obj.ToString());
            } catch (IOException e) {
                throw new Exception("Unexpected IOException", e);
            }
        }

        /**
         * Writes a DOCTYPE.
         * @param rootElementName the root element name
         */
        private void WriteDocType(string rootElementName) {
            if (dtdFilePath == null && entityDefs.Count == 0) return;

            Write("<!DOCTYPE " + rootElementName);
            if (dtdFilePath != null) Write(" SYSTEM \"" + dtdFilePath + '"');

            if (entityDefs.Count > 0) {
                Write(" [");

                foreach (string entityDef in entityDefs)
                {
                    if (WillIndent()) Write('\n' + indent);
                    Write("<!ENTITY " + entityDef + '>');
                }

                if (WillIndent()) Write('\n');
                Write(']');

                entityDefs.Clear();
            }

            Write('>');
            if (WillIndent()) Write('\n');
        }

        /**
         * Writes the proper amount of indentation
         * given the current nesting of elements.
         */
        private void WriteIndent() {
            if (!WillIndent()) return;

            Write('\n');
            int size = parentStack.Count;
            for (int i = 0; i < size; ++i) Write(indent);
        }

        /**
         * Writes the namespace declaration for the XMLSchema-instance namespace
         * and writes the schemaLocation attribute
         * which associates namespace URIs with schema locations.
         */
        private void WriteSchemaLocations() {
            if (namespaceURIToSchemaPathMap.Count == 0) return;

            // Write the attributes needed to associate XML Schemas
            // with this XML.
            string schemaLocation = "";
            foreach (string uri in namespaceURIToSchemaPathMap.Keys)
            {
                string path = namespaceURIToSchemaPathMap[uri];
                
                // If not the first pair output ...
                if (schemaLocation.Length > 0) {
                    if (WillIndent()) {
                        schemaLocation += '\n';
                        int size = parentStack.Count;
                        for (int i = 0; i <= size; ++i) {
                            schemaLocation += indent;
                        }
                    } else {
                        schemaLocation += ' ';
                    }
                }
                
                schemaLocation += uri + ' ' + path;
            }
            
            Namespace("xsi", XMLUtil.XMLSCHEMA_INSTANCE_NS);
            Attr(WillIndent(), "xsi", "schemaLocation", schemaLocation);
            attrOnNewLine = true; // for the next attribute
            namespaceURIToSchemaPathMap.Clear();
        }

        /**
         * Writes an XML declaration.
         * Note that regardless of indentation,
         * a newline is always written after this.
         * @param version the XML version
         */
        private void WriteXMLDeclaration(Version version) {
            if (version == Version.UNSPECIFIED) return;

            string versionstring =
                version == Version.V1_0 ? "1.0" :
                version == Version.V1_1 ? "1.1" :
                version == Version.V1_2 ? "1.2" :
                    null; // should never happen

            if (versionstring == null) {
                throw new ArgumentException("unsupported XML version");
            }

            Write("<?xml version=\"" + versionstring +
                "\" encoding=\"" + XMLUtil.DEFAULT_ENCODING + "\"?>\n");
        }

        /**
         * Writes an "xml-stylesheet" processing instruction.
         * @param filePath the path to the XSLT stylesheet
         * @return the calling object to support chaining
         */
        public PrologWAX Xslt(string filePath) {
            if (checkMe) {
                // EMMA incorrectly says this isn't called.
                if (state != State.IN_PROLOG) BadState("xslt");

                XMLUtil.VerifyURI(filePath);
            }

            return ProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"" + filePath + "\"");
        }
    }
}
