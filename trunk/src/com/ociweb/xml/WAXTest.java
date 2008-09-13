package com.ociweb.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

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

    private String getFileFirstLine(String filePath) throws IOException {
        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        br.close();
        return line;
    }

    @Test
    public void testAttributes() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.start("root").attr("a1", "v1").attr("a2", 2).close();

        assertEquals("<root a1=\"v1\" a2=\"2\"/>", sw.toString());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBadAttributeName() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").attr("1a", "value").close();
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
    public void testBadCloseAlreadyClosed() throws IOException {
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
        // Can't define same namespace prefix more than once in the same scope.
        wax.start("root")
           .namespace("tns", "http://www.ociweb.com/tns")
           .namespace("tns", "http://www.ociweb.com/tns")
           .close();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadNamespaceMultipleDefault() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        // Can't define default namespace more than once in the same scope.
        wax.start("root")
           .namespace("http://www.ociweb.com/tns1")
           .namespace("http://www.ociweb.com/tns2")
           .close();
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
        wax.child("foo", "child2", "two");
        wax.close();
    }

    @Test(expected=IllegalStateException.class)
    public void testBadText() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.text("text"); // haven't called start yet
    }

    @Test(expected=RuntimeException.class)
    public void testBadWrite() throws IOException {
        String filePath = "build/temp.xml";
        FileWriter fw = new FileWriter(filePath);
        WAX wax = new WAX(fw);
        wax.start("root");
        fw.close(); // closed the Writer instead of allowing WAX to do it
        new File(filePath).delete();
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

    @Test
    public void testBig() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root");
        wax.nlText("text #1");
        wax.child("child1", "text");
        wax.nlText("text #2");
        wax.start("child2").attr("a1", "v1").end();
        wax.nlText("text #3");
        wax.close();

        String xml = "<root>\n" + "  text #1\n" + "  <child1>text</child1>\n"
            + "  text #2\n" + "  <child2 a1=\"v1\"/>\n" + "  text #3\n"
            + "</root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testBlankLine() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").blankLine().close();

        String xml =
            "<root>\n" +
            "\n" +
            "</root>";
        assertEquals(xml, sw.toString());
    }
    
    @Test
    public void testCDATA() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.start("root").cdata("1<2>3&4'5\"6").close();

        String xml =
            "<root>\n" +
            "  <![CDATA[1<2>3&4'5\"6]]>\n" +
            "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testComment() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.comment("comment #1").comment("comment #2")
           .start("root").comment("comment #3").close();

        String xml =
            "<!-- comment #1 -->\n" +
            "<!-- comment #2 -->\n" +
            "<root>\n" +
            "  <!-- comment #3 -->\n" +
            "</root>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testDTD() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.dtd("http://www.ociweb.com/xml/root.dtd");
        wax.start("root");
        wax.close();

        String xml =
            "<!DOCTYPE root SYSTEM \"http://www.ociweb.com/xml/root.dtd\">\n" +
            "<root/>";

        assertEquals(xml, sw.toString());
    }

    @Test
    public void testEmpty() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.start("root").close();

        assertEquals("<root/>", sw.toString());
    }

    @Test
    public void testEntityDef() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.entityDef("name", "value").start("root").close();

        String xml = "<!DOCTYPE root [<!ENTITY name \"value\">]><root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testExternalEntityDef() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.externalEntityDef("name", "value").start("root").close();

        String xml = "<!DOCTYPE root [<!ENTITY name SYSTEM \"value\">]><root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testEscape() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.start("root").text("abc<def>ghi'jkl\"mno&pqr").close();

        String xml =
            "<root>abc&lt;def&gt;ghi&apos;jkl&quot;mno&amp;pqr</root>";
        assertEquals(xml, sw.toString());
    }

    @Test(expected=IllegalStateException.class)
    public void testExtraEnd() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.start("root").end().end().close();
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

        wax.setIndent(null);
        assertEquals(null, wax.getIndent());

        wax.setIndent("");
        assertEquals("", wax.getIndent());
    }

    @Test
    public void testIndentByNum() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(2);
        wax.start("root").child("child", "text").close();

        String xml = "<root>\n" + "  <child>text</child>\n" + "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testIndentByString() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent("  ");
        wax.start("root").child("child", "text").close();

        String xml = "<root>\n" + "  <child>text</child>\n" + "</root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testNamespace() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.start("root")
           .namespace("http://www.ociweb.com/tns1")
           .namespace("tns2", "http://www.ociweb.com/tns2")
           .namespace("tns3", "http://www.ociweb.com/tns3")
           .close();

        String xml = "<root" +
            " xmlns=\"http://www.ociweb.com/tns1\"" +
            " xmlns:tns2=\"http://www.ociweb.com/tns2\"" +
            " xmlns:tns3=\"http://www.ociweb.com/tns3\"/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testNoArgCtor() {
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //System.out = baos;
        WAX wax = new WAX();
        wax.start("root").close();
        //assertEquals("<root/>", baos.toString());
    }

    @Test
    public void testNoIndent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.start("root").child("child", "text").close();

        String xml = "<root><child>text</child></root>";
        assertEquals(xml, sw.toString());
    }

    @Test(expected=IllegalStateException.class)
    public void testNoRoot() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.close();
    }

    @Test
    public void testProcessingInstructionInPrologue() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.processingInstruction("xml-stylesheet",
            "type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"");
        wax.start("root");
        wax.close();

        String xml =
            "<?xml-stylesheet type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"?>\n" +
            "<root/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testProcessingInstructionAfterPrologue() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.start("root");
        wax.processingInstruction("target", "data");
        wax.close();

        String xml = "<root><?target data?></root>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testPrefix() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
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
           .namespace(null, "http://www.ociweb.com/tns1", "tns1.xsd")
           .namespace("tns2", "http://www.ociweb.com/tns2", "tns2.xsd")
           .close();

        String xml = "<root\n" +
            "  xmlns=\"http://www.ociweb.com/tns1\"\n" +
            "  xmlns:tns2=\"http://www.ociweb.com/tns2\"\n" +
            "  xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"\n" +
            "  xsi:schemaLocation=\"" +
            "http://www.ociweb.com/tns1 tns1.xsd\n" +
            "    http://www.ociweb.com/tns2 tns2.xsd" +
            "\"/>";
        assertEquals(xml, sw.toString());
    }

    @Test
    public void testSchemasWithoutIndent() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.start("root")
           .namespace(null, "http://www.ociweb.com/tns1", "tns1.xsd")
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
    public void testText() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setIndent(null);
        wax.start("root").text("text").close();

        assertEquals("<root>text</root>", sw.toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testTrustMeFalse() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setTrustMe(false);
        // Since error checking is turned on,
        // element names must be valid and text is escaped.
        wax.start("123").text("<>&'\"").close();
    }

    @Test
    public void testTrustMeTrue() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw);
        wax.setTrustMe(true);
        wax.setIndent(null);
        // Since error checking is turned off,
        // invalid element names and unescaped text are allowed.
        wax.start("123").text("<>&'\"").close();

        assertEquals("<123><>&'\"</123>", sw.toString());
    }

    @Test
    public void testWriteFile() throws IOException {
        String filePath = "build/temp.xml";
        WAX wax = new WAX(filePath);
        wax.setIndent(null);
        wax.start("root").text("text").close();
        assertEquals("<root>text</root>", getFileFirstLine(filePath));
        new File(filePath).delete();
    }

    @Test
    public void testWriteStream() throws IOException {
        String filePath = "build/temp.xml";
        FileOutputStream fos = new FileOutputStream(filePath);
        WAX wax = new WAX(fos);
        wax.setIndent(null);
        wax.start("root").text("text").close();
        assertEquals("<root>text</root>", getFileFirstLine(filePath));
        new File(filePath).delete();
    }

    @Test
    public void testXMLDeclaration() {
        StringWriter sw = new StringWriter();
        WAX wax = new WAX(sw, WAX.Version.V1_0);
        wax.setIndent(null);
        wax.start("root").close();

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>";
        assertEquals(xml, sw.toString());
    }
}
