using System;
using System.IO;
using System.Collections.Generic;
using System.Text;
using WAXNamespace;
using NUnit.Framework;

namespace WaxTest
{
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
    [TestFixture]
    public class WAXTest
    {

        private string getFileFirstLine(string filePath) /* throws IOException */ {
            Stream inputStream = File.OpenRead(filePath);
            StreamReader streamReader = new StreamReader(inputStream);
            string line = streamReader.ReadLine();
            streamReader.Close();
            return line;
        }

        [Test]
        public void testAttributes() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.start("root").attr("a1", "v1").attr("a2", 2).Close();

            Assert.AreEqual("<root a1=\"v1\" a2=\"2\"/>", sw.ToString());
        }
        
        [Test] //(expected=IllegalArgumentException.class)
        public void testBadAttributeName() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.start("root").attr("1a", "value").Close();

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=IllegalStateException.class)
        public void testBadCDATA() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                // Can't call cdata while in prologue section.
                wax.cdata("text").Close();

                Assert.Fail("Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
            }
        }

        [Test] //(expected=IllegalStateException.class)
        public void testBadChild() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.start("root");
                wax.end();
                wax.child("child", "text"); // can't call child after root is closed

                Assert.Fail("Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
            }
        }

        [Test] //(expected=IllegalStateException.class)
        public void testBadCloseWithoutRoot() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Close(); // didn't write anything yet

                Assert.Fail("Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
            }
        }

        [Test] //(expected=IllegalStateException.class)
        public void testBadCloseAlreadyClosed() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.start("root");
                wax.Close();
                wax.Close(); // already closed

                Assert.Fail("Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
            }
        }

        [Test] //(expected=IllegalStateException.class)
        public void testBadCloseThenWrite() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.start("root");
                wax.Close();
                wax.start("more"); // already closed

                Assert.Fail("Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
            }
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testBadComment() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.start("root").comment("foo--bar").Close();

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=RuntimeException.class)
        public void testBadDTDAfterRoot() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.start("root");
                wax.dtd("root.dtd"); // can't specify DTD after root element

                Assert.Fail("Expecting IOException (in Exception)");
            }
            catch (Exception ex)
            {
            }
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testBadElementName() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.start("1root").Close();

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=IllegalStateException.class)
        public void testBadEnd() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.end(); // haven't called start yet

                Assert.Fail("Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
            }
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testBadIndentBadChars() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.setIndent("abc"); // must be null, "", spaces or a single tab

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testBadIndentNegative() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.setIndent(-1); // must be >= 0

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testBadIndentMultipleTabs() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.setIndent("\t\t"); // more than one tab

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testBadIndentTooLarge() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.setIndent(5); // must be <= 4

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testBadNamespaceDuplicatePrefix() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                // Can't define same namespace prefix more than once in the same scope.
                wax.start("root")
                   .Namespace("tns", "http://www.ociweb.com/tns")
                   .Namespace("tns", "http://www.ociweb.com/tns")
                   .Close();

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testBadNamespaceMultipleDefault() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                // Can't define default namespace more than once in the same scope.
                wax.start("root")
                   .Namespace("http://www.ociweb.com/tns1")
                   .Namespace("http://www.ociweb.com/tns2")
                   .Close();

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testBadPrefix() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.start("root");
                wax.start("parent");
                wax.Namespace("foo", "http://www.ociweb.com/foo");
                wax.child("foo", "child1", "one");
                wax.end();
                wax.child("foo", "child2", "two");
                wax.Close();

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test] //(expected=IllegalStateException.class)
        public void testBadText() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Text("text"); // haven't called start yet

                Assert.Fail("Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
            }
        }

        [Test] //(expected=RuntimeException.class)
        public void testBadWrite() /* throws IOException */ {
            try {
                string filePath = "/tmp/temp.xml";
                FileStream fs = File.OpenWrite(filePath);
                WAX wax = new WAX(fs);
                wax.start("root");
                fs.Close(); // closed the Writer instead of allowing WAX to do it
                File.Delete(filePath);
                wax.Close(); // attempting to write more after the Writer was closed

                Assert.Fail("Expecting IOException (in Exception)");
            } catch (Exception ex) {
            }
        }

        [Test] //(expected=RuntimeException.class)
        public void testBadWriteFile() /* throws IOException */ {
            try {
                string filePath = "."; // the current directory, not a file
                new WAX(filePath);

                Assert.Fail("Expecting IOException (in Exception)");
            }
            catch (Exception ex)
            {
            }
        }

        [Test] //(expected=RuntimeException.class)
        public void testBadXSLTAfterRoot() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.start("root");
                wax.xslt("root.xslt"); // can't write pi after root element

                Assert.Fail("Expecting IOException (in Exception)");
            }
            catch (Exception ex)
            {
            }
        }

        [Test]
        public void testBig() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.start("root");
            wax.nlText("text #1");
            wax.child("child1", "text");
            wax.nlText("text #2");
            wax.start("child2").attr("a1", "v1").end();
            wax.nlText("text #3");
            wax.Close();

            string xml = "<root>\n" + "  text #1\n" + "  <child1>text</child1>\n"
                + "  text #2\n" + "  <child2 a1=\"v1\"/>\n" + "  text #3\n"
                + "</root>";

            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testBlankLine() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.start("root").blankLine().Close();

            string xml =
                "<root>\n" +
                "\n" +
                "</root>";
            Assert.AreEqual(xml, sw.ToString());
        }
        
        [Test]
        public void testCDATA() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.start("root").cdata("1<2>3&4'5\"6").Close();

            string xml =
                "<root>\n" +
                "  <![CDATA[1<2>3&4'5\"6]]>\n" +
                "</root>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testComment() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.comment("comment #1").comment("comment #2")
               .start("root").comment("comment #3").Close();

            string xml =
                "<!-- comment #1 -->\n" +
                "<!-- comment #2 -->\n" +
                "<root>\n" +
                "  <!-- comment #3 -->\n" +
                "</root>";

            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testDTD() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.dtd("http://www.ociweb.com/xml/root.dtd");
            wax.start("root");
            wax.Close();

            string xml =
                "<!DOCTYPE root SYSTEM \"http://www.ociweb.com/xml/root.dtd\">\n" +
                "<root/>";

            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testEmpty() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.start("root").Close();

            Assert.AreEqual("<root/>", sw.ToString());
        }

        [Test]
        public void testEntityDef() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.entityDef("name", "value").start("root").Close();

            string xml = "<!DOCTYPE root [<!ENTITY name \"value\">]><root/>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testExternalEntityDef() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.externalEntityDef("name", "value").start("root").Close();

            string xml = "<!DOCTYPE root [<!ENTITY name SYSTEM \"value\">]><root/>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testEscape() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.start("root").Text("abc<def>ghi'jkl\"mno&pqr").Close();

            string xml =
                "<root>abc&lt;def&gt;ghi&apos;jkl&quot;mno&amp;pqr</root>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test] //(expected=IllegalStateException.class)
        public void testExtraEnd() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.setIndent(null);
                wax.start("root").end().end().Close();

                Assert.Fail("Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
            }
        }

        [Test]
        public void testGetIndent() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            Assert.AreEqual("  ", wax.getIndent());

            wax.setIndent("    ");
            Assert.AreEqual("    ", wax.getIndent());

            wax.setIndent("\t");
            Assert.AreEqual("\t", wax.getIndent());

            wax.setIndent(null);
            Assert.AreEqual(null, wax.getIndent());

            wax.setIndent("");
            Assert.AreEqual("", wax.getIndent());
        }

        [Test]
        public void testIndentByNum() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(2);
            wax.start("root").child("child", "text").Close();

            string xml = "<root>\n" + "  <child>text</child>\n" + "</root>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testIndentBystring() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent("  ");
            wax.start("root").child("child", "text").Close();

            string xml = "<root>\n" + "  <child>text</child>\n" + "</root>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testnamespace_() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.start("root")
               .Namespace("http://www.ociweb.com/tns1")
               .Namespace("tns2", "http://www.ociweb.com/tns2")
               .Namespace("tns3", "http://www.ociweb.com/tns3")
               .Close();

            string xml = "<root" +
                " xmlns=\"http://www.ociweb.com/tns1\"" +
                " xmlns:tns2=\"http://www.ociweb.com/tns2\"" +
                " xmlns:tns3=\"http://www.ociweb.com/tns3\"/>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testNoArgCtor() {
            TextWriter oldConsoleOut = Console.Out;
            StringWriter sw = new StringWriter();
            Console.SetOut(sw);
            try
            {
                WAX wax = new WAX();
                wax.start("root").Close();
                Assert.AreEqual("<root/>", sw.ToString());
            }
            finally
            {
                Console.SetOut(oldConsoleOut);
            }
        }

        [Test]
        public void testNoIndent() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.start("root").child("child", "text").Close();

            string xml = "<root><child>text</child></root>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test] //(expected=IllegalStateException.class)
        public void testNoRoot() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Close();

                Assert.Fail("Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
            }
        }

        [Test]
        public void testProcessingInstructionInPrologue() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.processingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"");
            wax.start("root");
            wax.Close();

            string xml =
                "<?xml-stylesheet type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"?>\n" +
                "<root/>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testProcessingInstructionAfterPrologue() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.start("root");
            wax.processingInstruction("target", "data");
            wax.Close();

            string xml = "<root><?target data?></root>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testPrefix() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.start("foo", "root")
               .attr("bar", "baz")
               // Note that the namespace is defined after it is used,
               // but on the same element, which should be allowed.
               .Namespace("foo", "http://www.ociweb.com/foo")
               .Close();

            string xml = "<foo:root bar=\"baz\" " +
                "xmlns:foo=\"http://www.ociweb.com/foo\"/>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testSchemasWithIndent() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.start("root")
               .Namespace(null, "http://www.ociweb.com/tns1", "tns1.xsd")
               .Namespace("tns2", "http://www.ociweb.com/tns2", "tns2.xsd")
               .Close();

            string xml = "<root\n" +
                "  xmlns=\"http://www.ociweb.com/tns1\"\n" +
                "  xmlns:tns2=\"http://www.ociweb.com/tns2\"\n" +
                "  xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"\n" +
                "  xsi:schemaLocation=\"http://www.ociweb.com/tns1 tns1.xsd\n" +
                "    http://www.ociweb.com/tns2 tns2.xsd" +
                "\"/>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testSchemasWithoutIndent() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.start("root")
               .Namespace(null, "http://www.ociweb.com/tns1", "tns1.xsd")
               .Namespace("tns2", "http://www.ociweb.com/tns2", "tns2.xsd")
               .Close();

            string xml = "<root" +
                " xmlns=\"http://www.ociweb.com/tns1\"" +
                " xmlns:tns2=\"http://www.ociweb.com/tns2\"" +
                " xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"" +
                " xsi:schemaLocation=\"" +
                "http://www.ociweb.com/tns1 tns1.xsd " +
                "http://www.ociweb.com/tns2 tns2.xsd" +
                "\"/>";
            Assert.AreEqual(xml, sw.ToString());
        }

        [Test]
        public void testText() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setIndent(null);
            wax.start("root").Text("text").Close();

            Assert.AreEqual("<root>text</root>", sw.ToString());
        }

        [Test] //(expected=IllegalArgumentException.class)
        public void testTrustMeFalse() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.setTrustMe(false);
                // Since error checking is turned on,
                // element names must be valid and text is escaped.
                wax.start("123").Text("<>&'\"").Close();

                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
            }
        }

        [Test]
        public void testTrustMeTrue() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.setTrustMe(true);
            wax.setIndent(null);
            // Since error checking is turned off,
            // invalid element names and unescaped text are allowed.
            wax.start("123").Text("<>&'\"").Close();

            Assert.AreEqual("<123><>&'\"</123>", sw.ToString());
        }

        [Test]
        public void testWriteFile() /* throws IOException */ {
            string filePath = "/tmp/temp.xml";
            WAX wax = new WAX(filePath);
            wax.setIndent(null);
            wax.start("root").Text("text").Close();
            Assert.AreEqual("<root>text</root>", getFileFirstLine(filePath));
            File.Delete(filePath);
        }

        [Test]
        public void testWriteStream() /* throws IOException */ {
            string filePath = "/tmp/temp.xml";
            FileStream fs = File.OpenWrite(filePath);
            WAX wax = new WAX(fs);
            wax.setIndent(null);
            wax.start("root").Text("text").Close();
            Assert.AreEqual("<root>text</root>", getFileFirstLine(filePath));
            File.Delete(filePath);
        }

        [Test]
        public void testXMLDeclaration() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw, WAX.Version.V1_0);
            wax.setIndent(null);
            wax.start("root").Close();

            string xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>";
            Assert.AreEqual(xml, sw.ToString());
        }
    }
}
