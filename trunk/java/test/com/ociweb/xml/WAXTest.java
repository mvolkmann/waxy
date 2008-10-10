package com.ociweb.xml;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import static org.junit.Assert.*;

/**
 * This class provides unit test methods for WAX.
 * 
 * Copyright 2008 R. Mark Volkmann
 * 
 * This file is part of WAX.
 *
 * WAX is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with WAX.  If not, see http://www.gnu.org/licenses.
 * 
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public class WAXTest {

    private interface RunnableThrowsException {
        void run() throws Exception;
    }

    private static void assertStringContains(final String expectedSubstring,
            final String actualStringValue) {
        final String assertionErrorMessage = "Expected string\n" //
                + "   <" + actualStringValue + ">\n" //
                + " to contain the substring\n" //
                + "   <" + expectedSubstring + ">";
        assertTrue(assertionErrorMessage, actualStringValue
                .indexOf(expectedSubstring) > -1);
    }

    private static String captureSystemErrorOutput(
            final RunnableThrowsException runnable) throws Exception {
        final PrintStream originalSystemErrorOutput = System.err;
        try {
            final ByteArrayOutputStream fakeSystemErrorOutput = new ByteArrayOutputStream();
            System.setErr(new PrintStream(fakeSystemErrorOutput));

            runnable.run();

            return fakeSystemErrorOutput.toString();
        } finally {
            System.setErr(originalSystemErrorOutput);
        }
    }

    private static String getFileFirstLine(File file) throws IOException {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        try {
            line = br.readLine();
        } finally {
            br.close();
        }
        return line;
    }

    private static File getWAXTempXMLFile() throws IOException {
        return File.createTempFile("WAXTest", ".xml");
    }

    private static Document parseXml(final String xmlString)
            throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.parse(new ByteArrayInputStream(xmlString
                .getBytes()));
        return doc;
    }

    @Test
    public void testAttributes() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root")
           .namespace("foo", "http://www.ociweb.com/foo")
           .attr("a1", "v1")
           .attr("a2", 2)
           .attr("foo", "a3", "bar")
           .attr("foo", "a4", "baz", true)
           .close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root" + lineSeparator +
            "  xmlns:foo=\"http://www.ociweb.com/foo\"" + lineSeparator +
            "  a1=\"v1\"" + lineSeparator +
            "  a2=\"2\"" + lineSeparator +
            "  foo:a3=\"bar\"" + lineSeparator +
            "  foo:a4=\"baz\"/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testAttributeWithEscape() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").attr("a", "1&2").close();
        String xml = "<root a=\"1&amp;2\"/>";
        assertEquals(xml, sw.toString());
    }
    
    @Test
    public void testAttributeWithoutEscape() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").unescapedAttr("a", "1&2").close();
        String xml = "<root a=\"1&2\"/>";
        assertEquals(xml, sw.toString());
    }

    /**
     * From <a href="http://www.w3.org/TR/2008/PER-xml-20080205/#sec-starttags">
     * Extensible Markup Language (XML) 1.0 (Fifth Edition) -- 3.1 Start-Tags,
     * End-Tags, and Empty-Element Tags</a>:
     * <p>
     * <b>Well-formedness constraint: Unique Att Spec</b>
     * </p>
     * <p>
     * An attribute name <em class="rfc2119"
     * title="Keyword in RFC 2119 context">MUST NOT</em> appear more than once
     * in the same start-tag or empty-element tag.
     * </p>
     */
    @Test
    public void testBadAttribute_DuplicateName() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.attr("name", "value1");
        try {
            wax.attr("name", "value2");
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals("The attribute \"name\" is defined twice in this element.",
                    expectedIllegalArgumentException.getMessage());
        }
    }

    /**
     * Covered by same spec as <code>testBadAttribute_DuplicateName</code>
     * method above.
     */
    @Test
    public void testBadAttribute_DuplicateQualifiedName() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.attr("ns", "attr", "value1");
        try {
            wax.attr("ns", "attr", "value2");
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals("The attribute \"ns:attr\" is defined twice in this element.",
                    expectedIllegalArgumentException.getMessage());
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadAttributeName() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").attr("1a", "value").close();
    }

    @Test
    public void testBadAttributePrefixDetectedInCloseCall() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.start("parent");
        wax.attr("foo", "child2", "two");
        try {
            wax.close();
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals("The namespace prefix \"foo\" isn't in scope.",
                    expectedIllegalArgumentException.getMessage());
        }
    }

    @Test
    public void testBadAttributePrefixDetectedInEndCall() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.start("parent");
        wax.attr("foo", "child2", "two");
        try {
            wax.end();
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals("The namespace prefix \"foo\" isn't in scope.",
                    expectedIllegalArgumentException.getMessage());
        }
        try {
            wax.close();
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals("The namespace prefix \"foo\" isn't in scope.",
                    expectedIllegalArgumentException.getMessage());
        }
    }

    @Test(expected=IllegalStateException.class)
    public void testBadAttributeTimingCaught() {
        WAX wax = new WAX();
        wax.start("root");
        wax.text("text");
        // Can't call "attr" after calling "text".
        wax.attr("a1", "v1");
    }

    /*
    @Test(expected=RuntimeException.class)
    public void testBadCloseAlreadyClosedByCaller() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        sw.close();
        // Note that the Writer doesn't throw an exception in the next line!
        wax.close(); // the Writer is already closed
    }
    */

    @Test(expected=IllegalStateException.class)
    public void testBadCDATA() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        // Can't call cdata while in prologue section.
        wax.cdata("text");
    }

    @Test(expected=IllegalStateException.class)
    public void testBadChild() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.end();
        wax.child("child", "text"); // can't call child after root is closed
    }

    @Test(expected=IllegalStateException.class)
    public void testBadCloseAlreadyClosedByWAX() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.close();
        wax.close(); // already closed
    }

    @Test(expected=IllegalStateException.class)
    public void testBadCloseThenWrite() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.close();
        wax.start("more"); // already closed
    }

    @Test(expected=IllegalStateException.class)
    public void testBadCloseWithoutRoot() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.close(); // didn't write anything yet
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadComment() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").comment("foo--bar").close();
    }

    @Test(expected=IllegalStateException.class)
    public void testBadDTDAfterRoot() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.dtd("root.dtd"); // can't specify DTD after root element
    }

    @Test(expected=IllegalStateException.class)
    public void testBadDTDMultiple() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.dtd("one.dtd");
        wax.dtd("two.dtd"); // can't specify two DTDs
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadElementName() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("1root");
    }

    @Test(expected=IllegalStateException.class)
    public void testBadEnd() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.end(); // haven't called start yet
    }

    @Test(expected=IllegalStateException.class)
    public void testBadEntityDef() {
        WAX wax = new WAX();
        // Can't define an entity after the root element start tag.
        wax.start("root");
        wax.entityDef("name", "value");
    }

    @Test(expected=IllegalStateException.class)
    public void testBadExtraEnd() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").end().end();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIndentBadChars() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent("abc"); // must be null, "", spaces or a single tab
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIndentMultipleTabs() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent("\t\t"); // more than one tab
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIndentNegative() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(-1); // must be >= 0
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIndentTooLarge() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(5); // must be <= 4
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadNamespaceDuplicatePrefix() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        // Can't define same namespace prefix more
        // than once on the same element.
        wax.start("root")
           .namespace("tns", "http://www.ociweb.com/tns")
           .namespace("tns", "http://www.ociweb.com/tns");
    }

    @Test(expected=IllegalStateException.class)
    public void testBadNamespaceInElementContent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").text("text");
        wax.namespace("tns", "http://www.ociweb.com/tns");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadNamespaceMultipleDefault() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        // Can't define default namespace more than once
        // on the same element.
        wax.start("root")
           .defaultNamespace("http://www.ociweb.com/tns1")
           .defaultNamespace("http://www.ociweb.com/tns2");
    }

    @Test(expected=IllegalStateException.class)
    public void testBadNoRoot() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.close();
    }

    @Test
    public void testBadPrefix_NamespaceWentOutOfScope() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.start("parent");
        wax.namespace("foo", "http://www.ociweb.com/foo");
        wax.child("foo", "child1", "one");
        wax.end();
        try {
            // The prefix "foo" is out of scope now.
            wax.child("foo", "child2", "two");
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals("The namespace prefix \"foo\" isn't in scope.",
                    expectedIllegalArgumentException.getMessage());
        }
        try {
            wax.close();
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals("The namespace prefix \"foo\" isn't in scope.",
                    expectedIllegalArgumentException.getMessage());
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadSetLineSeparator() {
        WAX wax = new WAX();
        wax.setLineSeparator("abc");
    }

    @Test(expected=IllegalStateException.class)
    public void testBadSetLineSeparatorTiming() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        // can't call after output has started
        wax.setLineSeparator(WAX.UNIX_LINE_SEPARATOR);
    }

    @Test(expected=IllegalStateException.class)
    public void testBadTextAfterRootEnd() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.end();
        // Can't output more text after root element is terminated.
        wax.text("text");
    }

    @Test(expected=IllegalStateException.class)
    public void testBadTextInProlog() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.text("text"); // haven't called start yet
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadTrustMeFalse() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setTrustMe(false);
        // Since error checking is turned on,
        // element names must be valid.
        wax.start("123");
    }

    @Test(expected=RuntimeException.class)
    public void testBadWrite() throws IOException {
        File tempXMLFile = getWAXTempXMLFile();
        Writer fw = new FileWriter(tempXMLFile.getAbsolutePath());
        WAX wax = new WAX(fw);
        try {
            wax.start("root");
            fw.close(); // closed the Writer instead of allowing WAX to do it
        } finally {
            boolean success = tempXMLFile.delete();
            assertTrue(success);
        }
        wax.close(); // attempting to write more after the Writer was closed
    }

    @Test(expected=RuntimeException.class)
    public void testBadWriteFile() throws IOException {
        String filePath = "."; // the current directory, not a file
        new WAX(filePath);
    }

    @Test(expected=RuntimeException.class)
    public void testBadXSLTAfterRoot() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.xslt("root.xslt"); // can't write this pi after root element
    }

    @Test(expected=IllegalStateException.class)
    public void testBadXSLTMultiple() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.xslt("one.xslt");
        wax.xslt("two.xslt"); // can't specify two XSLTs
    }

    @Test
    public void testBig() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.text("text #1", true);
        wax.child("child1", "text");
        wax.text("text #2", true);
        wax.start("child2").attr("a1", "v1").end();
        wax.text("text #3", true);
        wax.close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root>" + lineSeparator +
            "  text #1" + lineSeparator +
            "  <child1>text</child1>" + lineSeparator +
            "  text #2" + lineSeparator +
            "  <child2 a1=\"v1\"/>" + lineSeparator +
            "  text #3" + lineSeparator +
            "</root>";

        assertEquals(xml, sw.toString());
    }
    
    @Test
    public void testBlankLine() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").blankLine().close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root>" + lineSeparator +
            lineSeparator +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testCDATAWithNewLines() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").cdata("1<2>3&4'5\"6", true).close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root>" + lineSeparator +
            "  <![CDATA[1<2>3&4'5\"6]]>" + lineSeparator +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testCDATAWithoutNewLines() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").cdata("1<2>3&4'5\"6").close();

        String xml =
            "<root><![CDATA[1<2>3&4'5\"6]]></root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testCloseThrowsIOException() {
        final IOException testIOException = 
            new IOException("JUnit test exception");

        StringWriter sw = new StringWriter() {
            @Override
            public void close() throws IOException {
                super.close();
                throw testIOException;
            }
        };
        
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root");
        try {
            wax.close();
            fail("expecting RuntimeException containing IOException");
        } catch (RuntimeException e) {
            assertSame(testIOException, e.getCause());
        }
    }

    @Test
    public void testCommentedStartWithContent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);

        wax.start("root")
           .commentedStart("child")
           .child("grandchild", "some text")
           .close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root>" + lineSeparator +
            "  <!--child>" + lineSeparator +
            "    <grandchild>some text</grandchild>" + lineSeparator +
            "  </child-->" + lineSeparator +
            "</root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testCommentedStartWithNamespace() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root")
           .namespace("foo", "http://www.ociweb.com/foo")
           .commentedStart("foo", "child")
           .child("grandchild", "some text")
           .close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root" + lineSeparator +
            "  xmlns:foo=\"http://www.ociweb.com/foo\">" + lineSeparator +
            "  <!--foo:child>" + lineSeparator +
            "    <grandchild>some text</grandchild>" + lineSeparator +
            "  </foo:child-->" + lineSeparator +
            "</root>";
        
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testCommentedStartWithoutContent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);

        wax.start("root")
           .commentedStart("child1")
           .end()
           .child("child2", "some text")
           .close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root>" + lineSeparator +
            "  <!--child1/-->" + lineSeparator +
            "  <child2>some text</child2>" + lineSeparator +
            "</root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testCommentOnlyFileFails() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setTrustMe(true);
        wax.comment(null);
        try {
            wax.close();
            fail("Expecting IllegalStateException.");
        } catch (IllegalStateException expectedIllegalStateException) {
            assertEquals("can't call close when state is IN_PROLOG",
                    expectedIllegalStateException.getMessage());
        }
    }

    @Test
    public void testCommentWithNewLines() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.comment("comment #1", true).comment("comment #2", true)
           .start("root").comment("comment #3", true).close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<!--" + lineSeparator +
            "  comment #1" + lineSeparator +
            "-->" + lineSeparator +
            "<!--" + lineSeparator +
            "  comment #2" + lineSeparator +
            "-->" + lineSeparator +
            "<root>" + lineSeparator +
            "  <!--" + lineSeparator +
            "    comment #3" + lineSeparator +
            "  -->" + lineSeparator +
            "</root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testCommentWithoutNewLines() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.comment("comment #1").comment("comment #2")
           .start("root").comment("comment #3").close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<!-- comment #1 -->" + lineSeparator +
            "<!-- comment #2 -->" + lineSeparator +
            "<root>" + lineSeparator +
            "  <!-- comment #3 -->" + lineSeparator +
            "</root>";

        assertEquals(xml, sw.toString());
    }

    /**
     * From <a
     * href="http://www.w3.org/TR/2008/PER-xml-20080205/#syntax">Extensible
     * Markup Language (XML) 1.0 (Fifth Edition), W3C Proposed Edited
     * Recommendation 05 February 2008 - 2.4 Character Data and Markup:</a> <br>
     * "The right angle bracket (>) may be represented using the string
     * '&amp;gt;', and MUST, <a
     * href="http://www.w3.org/TR/2008/PER-xml-20080205/#dt-compat">for
     * compatibility</a>, be escaped using either '&amp;gt;' or a character
     * reference when it appears in the string ']]>' in content, when that
     * string is not marking the end of a <a
     * href="http://www.w3.org/TR/2008/PER-xml-20080205/#dt-cdsection">CDATA
     * section</a>."
     */
    @Test
    public void testCompatibilityQuoteForCdataEndMarker() throws Exception {
        final StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").text("==]]>==").close();

        final String xmlString = sw.toString();
        assertEquals("<root>==]]&gt;==</root>", xmlString);

        final Document doc = parseXml(xmlString);
        doc.normalize();
        final Element rootElement = doc.getDocumentElement();
        assertEquals("root", rootElement.getNodeName());
        assertEquals("==]]>==", rootElement.getTextContent());
    }

    @Test
    public void testDefaultNamespace() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root")
           .defaultNamespace("http://www.ociweb.com/tns")
           .close();
        assertEquals("<root xmlns=\"http://www.ociweb.com/tns\"/>",
            sw.toString());
    }

    @Test
    public void testDefaultNamespaceWithSchema() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root")
           .defaultNamespace("http://www.ociweb.com/tns", "tns.xsd").close();
        assertEquals(
            "<root" +
            " xmlns=\"http://www.ociweb.com/tns\"" +
            " xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"" +
            " xsi:schemaLocation=\"http://www.ociweb.com/tns tns.xsd\"/>",
            sw.toString());
    }

    @Test
    public void testDefaultNS() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root").defaultNS("http://www.ociweb.com/tns").close();
        assertEquals("<root xmlns=\"http://www.ociweb.com/tns\"/>",
            sw.toString());
    }

    @Test
    public void testDefaultNSWithSchema() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root")
           .defaultNS("http://www.ociweb.com/tns", "tns.xsd")
           .close();
        assertEquals(
            "<root" +
            " xmlns=\"http://www.ociweb.com/tns\"" +
            " xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"" +
            " xsi:schemaLocation=\"http://www.ociweb.com/tns tns.xsd\"/>",
            sw.toString());
    }

    @Test
    public void testDTDPublic() {
        // Testing with the ids for the strict form of XHTML.
        String publicId = "-//W3C//DTD XHTML 1.0 Strict//EN";
        String systemId = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";

        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.dtd(publicId, systemId);
        wax.start("root");
        wax.close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<!DOCTYPE root PUBLIC \"" + publicId + "\" \"" +
            systemId  + "\">" + lineSeparator +
            "<root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testDTDSystem() {
        String systemId = "http://www.ociweb.com/xml/root.dtd";

        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.dtd(systemId);
        wax.start("root");
        wax.close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<!DOCTYPE root SYSTEM \"" + systemId  + "\">" + lineSeparator +
            "<root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testEmpty() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root").close();

        assertEquals("<root/>", sw.toString());
    }

    @Test
    public void testEndVerbose() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").end(true).close();

        String xml = "<root></root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testEntityDef() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.entityDef("name", "value").start("root").close();

        String xml = "<!DOCTYPE root [<!ENTITY name \"value\">]><root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testEscape() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root").text("abc<def>ghi'jkl\"mno&pqr").close();

        String xml =
            "<root>abc&lt;def&gt;ghi&apos;jkl&quot;mno&amp;pqr</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testEscapeOffAndOn() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root")
           .unescapedText("&")
           .text("&")
           .close();

        String xml = "<root>&&amp;</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testExternalEntityDef() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.externalEntityDef("name", "value").start("root").close();

        String xml = "<!DOCTYPE root [<!ENTITY name SYSTEM \"value\">]><root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testGetIndent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        assertEquals("  ", wax.getIndent());

        wax.setIndent("    ");
        assertEquals("    ", wax.getIndent());

        wax.setIndent("\t");
        assertEquals("\t", wax.getIndent());

        wax.noIndentsOrLineSeparators();
        assertEquals(null, wax.getIndent());

        wax.setIndent("");
        assertEquals("", wax.getIndent());
    }

    @Test
    public void testIllustrateParsingErrorWhenTextLooksLikeCdataCloseSequence()
            throws Exception {

        final String systemErrorOutput = captureSystemErrorOutput(new RunnableThrowsException() {
            public void run() throws Exception {
                try {
                    parseXml("<root>==]]>==</root>");

                    fail("Expecting SAXParseException.");
                } catch (final SAXParseException expectedSAXParseException) {
                    assertEquals(
                            "The character sequence \"]]>\" must not appear in content unless used to mark the end of a CDATA section.",
                            expectedSAXParseException.getMessage());
                }
            }
        });

        assertTrue(systemErrorOutput.startsWith("[Fatal Error] "));
        assertStringContains(
                "The character sequence \"]]>\" must not appear in content unless used to mark the end of a CDATA section.",
                systemErrorOutput);
    }

    @Test
    public void testIndentByNum() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(1);
        wax.start("root").child("child", "text").close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root>" + lineSeparator +
            " <child>text</child>" + lineSeparator +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testIndentByString() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);

        wax.setIndent(" "); // 1 space
        wax.start("root").child("child", "text").close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root>" + lineSeparator +
            " <child>text</child>" + lineSeparator +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testIndentByStringWeird() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);

        // Can set indent to anything if "trust me" is true.
        wax.setTrustMe(true);
        String indent = "abc";
        wax.setIndent(indent); // weird indentation characters
        wax.setTrustMe(false);

        wax.start("root").child("child", "text").close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<root>" + lineSeparator +
            indent + "<child>text</child>" + lineSeparator +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testNamespace() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root")
           .defaultNamespace("http://www.ociweb.com/tns1")
           .namespace("tns2", "http://www.ociweb.com/tns2")
           .ns("tns3", "http://www.ociweb.com/tns3")
           .close();

        String xml = "<root" +
            " xmlns=\"http://www.ociweb.com/tns1\"" +
            " xmlns:tns2=\"http://www.ociweb.com/tns2\"" +
            " xmlns:tns3=\"http://www.ociweb.com/tns3\"/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testNamespaceDuplicatePrefix() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();

        // Can define same namespace prefix more than once
        // on different elements.
        String prefix = "tns";
        String uri = "http://www.ociweb.com/tns";
        wax.start("root").namespace(prefix, uri + "1")
           .start("child").namespace(prefix, uri + "2")
           .close();

        String xml =
            "<root xmlns:" + prefix + "=\"" + uri + "1\">" +
            "<child xmlns:" + prefix + "=\"" + uri + "2\"/>" +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testNamespaceMultipleDefault() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();

        // Can define default namespace prefix more than once
        // on different elements.
        String uri = "http://www.ociweb.com/tns";
        wax.start("root").defaultNamespace(uri + "1")
           .start("child").defaultNamespace(uri + "2")
           .close();

        String xml =
            "<root xmlns=\"" + uri + "1\">" +
            "<child xmlns=\"" + uri + "2\"/>" +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testNoApersignOrLessThanQuotingInComment() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root").comment("1&2<3").close();
        assertEquals("<root><!-- 1&2<3 --></root>", sw.toString());
    }

    @Test
    public void testNoApersignOrLessThanQuotingInProcessingInstruction() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setTrustMe(true);
        wax.noIndentsOrLineSeparators();
        wax.start("root").processingInstruction("1&2<3", "3&2<1").close();
        assertEquals("<root><?1&2<3 3&2<1?></root>", sw.toString());
    }

    @Test
    public void testNoArgCtor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalSystemOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            WAX wax = new WAX();
            wax.start("root").close();
            assertEquals("<root/>", baos.toString());
        } finally {
            System.setOut(originalSystemOut);
        }
    }

    @Test
    public void testNoIndent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root").child("child", "text").close();

        String xml = "<root><child>text</child></root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testNoIndentOrCRs() {
        WAX wax = new WAX();
        wax.noIndentsOrLineSeparators();
        assertEquals(null, wax.getIndent());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullComment() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.comment(null);
    }

    @Test
    public void testNullCommentAfterClose() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").close();
        wax.setTrustMe(true);
        try {
            wax.comment(null, true);
            fail("Expecting IllegalStateException.");
        } catch (IllegalStateException expectedIllegalStateException) {
            assertEquals("attempting to write XML after close has been called",
                    expectedIllegalStateException.getMessage());
        }
        assertEquals("<root/>", sw.toString());
    }

    @Test
    public void testNullCommentWithTrustMe() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setTrustMe(true);

        wax.comment(null);

        wax.start("root").close();
        String lineSeparator = wax.getLineSeparator();
        assertEquals("<!-- null -->" + lineSeparator + "<root/>", sw.toString());
    }

    @Test
    public void testPrefix() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("foo", "root")
           .attr("bar", "baz")
           // Note that the namespace is defined after it is used,
           // but on the same element, which should be allowed.
           .namespace("foo", "http://www.ociweb.com/foo")
           .close();

        String xml = "<foo:root bar=\"baz\" " +
            "xmlns:foo=\"http://www.ociweb.com/foo\"/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testProcessingInstructionAfterPrologue() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root");
        wax.processingInstruction("target1", "data1");
        wax.pi("target2", "data2");
        wax.close();

        String xml = "<root><?target1 data1?><?target2 data2?></root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testProcessingInstructionInPrologue() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.processingInstruction("xml-stylesheet",
            "type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"");
        wax.start("root");
        wax.close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<?xml-stylesheet type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"?>" + lineSeparator +
            "<root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testSchemasWithIndent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root")
           .defaultNamespace("http://www.ociweb.com/tns1", "tns1.xsd")
           .namespace("tns2", "http://www.ociweb.com/tns2", "tns2.xsd")
           .ns("tns3", "http://www.ociweb.com/tns3", "tns3.xsd")
           .close();

        String lineSeparator = wax.getLineSeparator();
        String xml = "<root" + lineSeparator +
            "  xmlns=\"http://www.ociweb.com/tns1\"" + lineSeparator +
            "  xmlns:tns2=\"http://www.ociweb.com/tns2\"" + lineSeparator +
            "  xmlns:tns3=\"http://www.ociweb.com/tns3\"" + lineSeparator +
            "  xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"" + lineSeparator +
            "  xsi:schemaLocation=\"http://www.ociweb.com/tns1 tns1.xsd" + lineSeparator +
            "    http://www.ociweb.com/tns2 tns2.xsd" + lineSeparator +
            "    http://www.ociweb.com/tns3 tns3.xsd\"/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testSchemasWithoutIndent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root")
           .defaultNamespace("http://www.ociweb.com/tns1", "tns1.xsd")
           .namespace("tns2", "http://www.ociweb.com/tns2", "tns2.xsd")
           .close();

        String xml = "<root" +
            " xmlns=\"http://www.ociweb.com/tns1\"" +
            " xmlns:tns2=\"http://www.ociweb.com/tns2\"" +
            " xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"" +
            " xsi:schemaLocation=\"" +
            "http://www.ociweb.com/tns1 tns1.xsd " +
            "http://www.ociweb.com/tns2 tns2.xsd" +
            "\"/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testSetLineSeparator() {
        WAX wax = new WAX();
        wax.setLineSeparator(WAX.UNIX_LINE_SEPARATOR);
        assertEquals(WAX.UNIX_LINE_SEPARATOR, wax.getLineSeparator());

        // Most of the other tests verify that
        // this CR is actually used in the output.
    }

    @Test
    public void testSpaceInEmptyElements() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();

        wax.start("root").start("child1").end();
        assertFalse(wax.isSpaceInEmptyElements());
        wax.setSpaceInEmptyElements(true);
        assertTrue(wax.isSpaceInEmptyElements());
        wax.start("child2").end();
        wax.setSpaceInEmptyElements(false);
        assertFalse(wax.isSpaceInEmptyElements());
        wax.start("child3").close();

        String xml = "<root><child1/><child2 /><child3/></root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testText() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrLineSeparators();
        wax.start("root").text("text").close();

        assertEquals("<root>text</root>", sw.toString());
    }

    @Test
    public void testTrustMeTrue() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        assertFalse(wax.isTrustMe());
        wax.setTrustMe(true);
        assertTrue(wax.isTrustMe());
        wax.noIndentsOrLineSeparators();
        // Since error checking is turned off,
        // invalid element names are allowed.
        wax.start("123").unescapedText("<>&'\"").close();

        assertEquals("<123><>&'\"</123>", sw.toString());
    }

    @Test
    public void testUseWindowsLineSeparator() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setLineSeparator(WAX.WINDOWS_LINE_SEPARATOR);
        wax.start("root")
           .child("child", "text")
           .close();

        String lineSeparator = wax.getLineSeparator();
        assertEquals(WAX.WINDOWS_LINE_SEPARATOR, lineSeparator);

        String xml = "<root>" + lineSeparator +
            "  <child>text</child>" + lineSeparator +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testWriteFile() throws IOException {
        File tempXMLFile = getWAXTempXMLFile();
        WAX wax = new WAX(tempXMLFile.getAbsolutePath());
        try {
            wax.noIndentsOrLineSeparators();
            wax.start("root").text("text").close();
            assertEquals("<root>text</root>", getFileFirstLine(tempXMLFile));
        } finally {
            boolean success = tempXMLFile.delete();
            assertTrue(success);
        }
    }

    @Test
    public void testWriteStream() throws IOException {
        File tempXMLFile = getWAXTempXMLFile();
        OutputStream fos = new FileOutputStream(tempXMLFile.getAbsolutePath());
        try {
            WAX wax = new WAX(fos);
            try {
                wax.noIndentsOrLineSeparators();
                wax.start("root").text("text").close();
                assertEquals("<root>text</root>",
                    getFileFirstLine(tempXMLFile));
            } finally {
                boolean success = tempXMLFile.delete();
                assertTrue(success);
            }
        } finally {
            fos.close();
        }
    }

    @Test
    public void testXMLDeclaration() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw, Version.V1_0);
        wax.noIndentsOrLineSeparators();
        wax.start("root").close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator +
            "<root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testXMLDeclarationNonDefaultEncoding()
    throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String encoding = "UTF-16";
        OutputStreamWriter osw = new OutputStreamWriter(baos, encoding);
        WAX wax = new WAX(osw, Version.V1_2);
        wax.noIndentsOrLineSeparators();
        wax.start("root").close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<?xml version=\"1.2\" encoding=\"" + encoding + "\"?>" + lineSeparator +
            "<root/>";
        assertEquals(xml, baos.toString(encoding));
    }

    @Test
    public void testXMLVersion11() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw, Version.V1_1);
        wax.start("root").close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<?xml version=\"1.1\" encoding=\"UTF-8\"?>" + lineSeparator +
            "<root/>";
        assertEquals(xml, sw.toString());
    }
    
    @Test
    public void testXMLVersionNull() {
        StringWriter sw = new StringWriter();
        try {
            new WAX(sw, null);
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals("unsupported XML version",
                expectedIllegalArgumentException.getMessage());
        }
    }

    @Test
    public void testXSLT() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.xslt("root.xslt");
        wax.start("root");
        wax.close();

        String lineSeparator = wax.getLineSeparator();
        String xml =
            "<?xml-stylesheet type=\"text/xsl\" href=\"root.xslt\"?>" + lineSeparator +
            "<root/>";

        assertEquals(xml, sw.toString());
    }
}
