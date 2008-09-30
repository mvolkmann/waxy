package com.ociweb.xml;

import java.io.*;
import org.junit.Test;
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

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String TEMP_XML_FILE_PATH = TEMP_DIR + File.separator + "temp.xml";

    private String getFileFirstLine(String filePath) throws IOException {
        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        try {
            line = br.readLine();
        } finally {
            br.close();
        }
        return line;
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

        String cr = wax.getCR();
        String xml =
            "<root" + cr +
            "  xmlns:foo=\"http://www.ociweb.com/foo\"" + cr +
            "  a1=\"v1\"" + cr +
            "  a2=\"2\"" + cr +
            "  foo:a3=\"bar\"" + cr +
            "  foo:a4=\"baz\"/>";
        assertEquals(xml, sw.toString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBadAttributeName() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").attr("1a", "value").close();
    }

    @Test(expected=IllegalStateException.class)
    public void testBadAttributeTimingCaught() {
        WAX wax = new WAX();
        wax.start("root");
        wax.text("text");
        // Can't call "attr" after calling "text".
        wax.attr("a1", "v1");
    }

    @Test(expected=IllegalStateException.class)
    public void testBadCDATA() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        // Can't call cdata while in prologue section.
        wax.cdata("text").close();
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
    public void testBadCloseWithoutRoot() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.close(); // didn't write anything yet
    }

    @Test(expected=IllegalStateException.class)
    public void testBadCloseAlreadyClosedByWAX() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.close();
        wax.close(); // already closed
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
    public void testBadCloseThenWrite() throws IOException {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.close();
        wax.start("more"); // already closed
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadComment() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").comment("foo--bar").close();
    }

    @Test(expected=RuntimeException.class)
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
        wax.start("1root").close();
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
        // Can't define and entity after the root element start tag.
        wax.start("root");
        wax.entityDef("name", "value");
    }

    @Test(expected=IllegalStateException.class)
    public void testBadExtraEnd() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").end().end().close();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIndentBadChars() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent("abc"); // must be null, "", spaces or a single tab
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIndentNegative() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(-1); // must be >= 0
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadIndentMultipleTabs() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent("\t\t"); // more than one tab
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
           .namespace("tns", "http://www.ociweb.com/tns")
           .close();
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
           .defaultNamespace("http://www.ociweb.com/tns2")
           .close();
    }

    @Test(expected=IllegalStateException.class)
    public void testBadNoRoot() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.close();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadPrefix() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.start("parent");
        wax.namespace("foo", "http://www.ociweb.com/foo");
        wax.child("foo", "child1", "one");
        wax.end();
        // The prefix "foo" is out of scope now.
        wax.child("foo", "child2", "two");
        wax.close();
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
        wax.start("123").close();
    }

    @Test(expected=IllegalStateException.class)
    public void testBadUseNonWindowsCR() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.useNonWindowsCR(); // can't call after output has started
    }

    @Test(expected=IllegalStateException.class)
    public void testBadUseWindowsCR() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.useWindowsCR(); // can't call after output has started
    }

    @Test(expected=RuntimeException.class)
    public void testBadWrite() throws IOException {
        Writer fw = new FileWriter(TEMP_XML_FILE_PATH);
        WAX wax = new WAX(fw);
        try {
            wax.start("root");
            fw.close(); // closed the Writer instead of allowing WAX to do it
        } finally {
            boolean success = new File(TEMP_XML_FILE_PATH).delete();
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
        wax.xslt("root.xslt"); // can't write pi after root element
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

        String cr = wax.getCR();
        String xml =
            "<root>" + cr +
            "  text #1" + cr +
            "  <child1>text</child1>" + cr +
            "  text #2" + cr +
            "  <child2 a1=\"v1\"/>" + cr +
            "  text #3" + cr +
            "</root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testBlankLine() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").blankLine().close();

        String cr = wax.getCR();
        String xml =
            "<root>" + cr +
            cr +
            "</root>";
        assertEquals(xml, sw.toString());
    }
    
    @Test
    public void testCDATAWithNewLines() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").cdata("1<2>3&4'5\"6", true).close();

        String cr = wax.getCR();
        String xml =
            "<root>" + cr +
            "  <![CDATA[1<2>3&4'5\"6]]>" + cr +
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
    public void testCommentWithNewLines() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.comment("comment #1", true).comment("comment #2", true)
           .start("root").comment("comment #3", true).close();

        String cr = wax.getCR();
        String xml =
            "<!--" + cr +
            "  comment #1" + cr +
            "-->" + cr +
            "<!--" + cr +
            "  comment #2" + cr +
            "-->" + cr +
            "<root>" + cr +
            "  <!--" + cr +
            "    comment #3" + cr +
            "  -->" + cr +
            "</root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testCommentWithoutNewLines() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.comment("comment #1").comment("comment #2")
           .start("root").comment("comment #3").close();

        String cr = wax.getCR();
        String xml =
            "<!-- comment #1 -->" + cr +
            "<!-- comment #2 -->" + cr +
            "<root>" + cr +
            "  <!-- comment #3 -->" + cr +
            "</root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testCommentedStartWithContent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);

        wax.start("root")
           .commentedStart("child")
           .child("grandchild", "some text")
           .close();

        String cr = wax.getCR();
        String xml =
            "<root>" + cr +
            "  <!--child>" + cr +
            "    <grandchild>some text</grandchild>" + cr +
            "  </child-->" + cr +
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

        String cr = wax.getCR();
        String xml =
            "<root>" + cr +
            "  <!--child1/-->" + cr +
            "  <child2>some text</child2>" + cr +
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

        String cr = wax.getCR();
        String xml =
            "<root" + cr +
            "  xmlns:foo=\"http://www.ociweb.com/foo\">" + cr +
            "  <!--foo:child>" + cr +
            "    <grandchild>some text</grandchild>" + cr +
            "  </foo:child-->" + cr +
            "</root>";
        
        assertEquals(xml, sw.toString());
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

        String cr = wax.getCR();
        String xml =
            "<!DOCTYPE root PUBLIC \"" + publicId + "\" \"" +
            systemId  + "\">" + cr +
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

        String cr = wax.getCR();
        String xml =
            "<!DOCTYPE root SYSTEM \"" + systemId  + "\">" + cr +
            "<root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testEmpty() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();
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
        wax.noIndentsOrCRs();
        wax.entityDef("name", "value").start("root").close();

        String xml = "<!DOCTYPE root [<!ENTITY name \"value\">]><root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testExternalEntityDef() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();
        wax.externalEntityDef("name", "value").start("root").close();

        String xml = "<!DOCTYPE root [<!ENTITY name SYSTEM \"value\">]><root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testEscape() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();
        wax.start("root").text("abc<def>ghi'jkl\"mno&pqr").close();

        String xml =
            "<root>abc&lt;def&gt;ghi&apos;jkl&quot;mno&amp;pqr</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testEscapeOffAndOn() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();
        wax.start("root")
           .unescapedText("&")
           .text("&")
           .close();

        String xml = "<root>&&amp;</root>";
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

        wax.noIndentsOrCRs();
        assertEquals(null, wax.getIndent());

        wax.setIndent("");
        assertEquals("", wax.getIndent());
    }

    @Test
    public void testIndentByNum() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(1);
        wax.start("root").child("child", "text").close();

        String cr = wax.getCR();
        String xml =
            "<root>" + cr +
            " <child>text</child>" + cr +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testIndentByString() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);

        wax.setIndent(" "); // 1 space
        wax.start("root").child("child", "text").close();

        String cr = wax.getCR();
        String xml =
            "<root>" + cr +
            " <child>text</child>" + cr +
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

        String cr = wax.getCR();
        String xml =
            "<root>" + cr +
            indent + "<child>text</child>" + cr +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testNamespace() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();
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
        wax.noIndentsOrCRs();

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
        wax.noIndentsOrCRs();

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
        wax.noIndentsOrCRs();
        wax.start("root").child("child", "text").close();

        String xml = "<root><child>text</child></root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testNoIndentOrCRs() {
        WAX wax = new WAX();
        wax.noIndentsOrCRs();
        assertEquals(null, wax.getIndent());
    }

    @Test
    public void testProcessingInstructionInPrologue() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.processingInstruction("xml-stylesheet",
            "type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"");
        wax.start("root");
        wax.close();

        String cr = wax.getCR();
        String xml =
            "<?xml-stylesheet type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"?>" + cr +
            "<root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testProcessingInstructionAfterPrologue() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();
        wax.start("root");
        wax.processingInstruction("target1", "data1");
        wax.pi("target2", "data2");
        wax.close();

        String xml = "<root><?target1 data1?><?target2 data2?></root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testPrefix() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();
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
    public void testSchemasWithIndent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root")
           .defaultNamespace("http://www.ociweb.com/tns1", "tns1.xsd")
           .namespace("tns2", "http://www.ociweb.com/tns2", "tns2.xsd")
           .ns("tns3", "http://www.ociweb.com/tns3", "tns3.xsd")
           .close();

        String cr = wax.getCR();
        String xml = "<root" + cr +
            "  xmlns=\"http://www.ociweb.com/tns1\"" + cr +
            "  xmlns:tns2=\"http://www.ociweb.com/tns2\"" + cr +
            "  xmlns:tns3=\"http://www.ociweb.com/tns3\"" + cr +
            "  xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"" + cr +
            "  xsi:schemaLocation=\"" +
            "http://www.ociweb.com/tns1 tns1.xsd" + cr +
            "    http://www.ociweb.com/tns2 tns2.xsd" + cr +
            "    http://www.ociweb.com/tns3 tns3.xsd" +
            "\"/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testSchemasWithoutIndent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();
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
    public void testSpaceInEmptyElements() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();

        wax.start("root").start("child1").end();
        wax.setSpaceInEmptyElements(true);
        wax.start("child2").end();
        wax.setSpaceInEmptyElements(false);
        wax.start("child3").close();

        String xml = "<root><child1/><child2 /><child3/></root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testText() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.noIndentsOrCRs();
        wax.start("root").text("text").close();

        assertEquals("<root>text</root>", sw.toString());
    }

    @Test
    public void testTrustMeTrue() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setTrustMe(true);
        wax.noIndentsOrCRs();
        // Since error checking is turned off,
        // invalid element names are allowed.
        wax.start("123").unescapedText("<>&'\"").close();

        assertEquals("<123><>&'\"</123>", sw.toString());
    }

    @Test
    public void testUseNonWindowsCR() {
        WAX wax = new WAX();
        wax.useNonWindowsCR();
        assertEquals("\n", wax.getCR());

        // Most of the other tests verify that
        // this CR is actually used in the output.
    }

    @Test
    public void testUseWindowsCR() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.useWindowsCR();
        wax.start("root")
           .child("child", "text")
           .close();

        String cr = wax.getCR();
        assertEquals("\r\n", cr);

        String xml = "<root>" + cr +
            "  <child>text</child>" + cr +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testWriteFile() throws IOException {
        WAX wax = new WAX(TEMP_XML_FILE_PATH);
        try {
            wax.noIndentsOrCRs();
            wax.start("root").text("text").close();
            assertEquals("<root>text</root>", getFileFirstLine(TEMP_XML_FILE_PATH));
        } finally {
            boolean success = new File(TEMP_XML_FILE_PATH).delete();
            assertTrue(success);
        }
    }

    @Test
    public void testWriteStream() throws IOException {
        OutputStream fos = new FileOutputStream(TEMP_XML_FILE_PATH);
        try {
            WAX wax = new WAX(fos);
            try {
                wax.noIndentsOrCRs();
                wax.start("root").text("text").close();
                assertEquals("<root>text</root>", getFileFirstLine(TEMP_XML_FILE_PATH));
            } finally {
                boolean success = new File(TEMP_XML_FILE_PATH).delete();
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
        wax.noIndentsOrCRs();
        wax.start("root").close();

        String cr = wax.getCR();
        String xml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + cr +
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
        wax.noIndentsOrCRs();
        wax.start("root").close();

        String cr = wax.getCR();
        String xml =
            "<?xml version=\"1.2\" encoding=\"" + encoding + "\"?>" + cr +
            "<root/>";
        assertEquals(xml, baos.toString(encoding));
    }

    @Test
    public void testXSLT() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.xslt("root.xslt");
        wax.start("root");
        wax.close();

        String cr = wax.getCR();
        String xml =
            "<?xml-stylesheet type=\"text/xsl\" href=\"root.xslt\"?>" + cr +
            "<root/>";

        assertEquals(xml, sw.toString());
    }
}
