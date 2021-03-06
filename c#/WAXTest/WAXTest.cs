using System;
using System.IO;
using WAXNamespace;
using Xunit;

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
    public class WAXTest
    {
        private string TEMP_XML_FILE_PATH = System.Environment.GetEnvironmentVariable("TEMP") + "/temp.xml";

        [Fact]
        public void testAttributes() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.Start("root").Attr("a1", "v1").Attr("a2", 2).Close();

            Assert.Equal("<root a1=\"v1\" a2=\"2\"/>", sw.ToString());
        }
        
        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadAttributeName() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Start("root").Attr("1a", "value").Close();

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("\"1a\" is an invalid NMTOKEN", ex.Message);
            }
        }

        [Fact] //(expected=IllegalStateException.class)
        public void testBadCDATA() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                // Can't call cdata while in prologue section.
                wax.CData("text").Close();

                Assert.True(false, "Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
                Assert.Equal("can't call cdata when state is IN_PROLOG", ex.Message);
            }
        }

        [Fact] //(expected=IllegalStateException.class)
        public void testBadChild() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Start("root");
                wax.End();
                wax.Child("child", "text"); // can't call child after root is closed

                Assert.True(false, "Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
                Assert.Equal("can't call child when state is AFTER_ROOT", ex.Message);
            }
        }

        [Fact] //(expected=IllegalStateException.class)
        public void testBadCloseWithoutRoot() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Close(); // didn't write anything yet

                Assert.True(false, "Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
                Assert.Equal("can't call close when state is IN_PROLOG", ex.Message);
            }
        }

        [Fact] //(expected=IllegalStateException.class)
        public void testBadCloseAlreadyClosed() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Start("root");
                wax.Close();
                wax.Close(); // already closed

                Assert.True(false, "Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
                Assert.Equal("already closed", ex.Message);
            }
        }

        [Fact] //(expected=IllegalStateException.class)
        public void testBadCloseThenWrite() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Start("root");
                wax.Close();
                wax.Start("more"); // already closed

                Assert.True(false, "Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
                Assert.Equal("can't call start when state is AFTER_ROOT", ex.Message);
            }
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadComment() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Start("root").Comment("foo--bar").Close();

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("\"foo--bar\" is an invalid comment", ex.Message);
            }
        }

        [Fact] //(expected=RuntimeException.class)
        public void testBadDTDAfterRoot() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Start("root");
                wax.Dtd("root.dtd"); // can't specify DTD after root element

                Assert.True(false, "Expecting IOException (in Exception)");
            }
            catch (Exception ex)
            {
                Assert.Equal("can't call dtd when state is IN_START_TAG", ex.Message);
            }
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadElementName() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Start("1root").Close();

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("\"1root\" is an invalid NMTOKEN", ex.Message);
            }
        }

        [Fact] //(expected=IllegalStateException.class)
        public void testBadEnd() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.End(); // haven't called start yet

                Assert.True(false, "Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
                Assert.Equal("can't call end when state is IN_PROLOG", ex.Message);
            }
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadIndentBadChars() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.SetIndent("abc"); // must be null, "", spaces or a single tab

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("invalid indent value", ex.Message);
            }
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadIndentNegative() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.SetIndent(-1); // must be >= 0

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("can't indent a negative number of spaces", ex.Message);
            }
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadIndentMultipleTabs() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.SetIndent("\t\t"); // more than one tab

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("invalid indent value", ex.Message);
            }
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadIndentTooLarge() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.SetIndent(5); // must be <= 4

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("5 is an unreasonable indentation", ex.Message);
            }
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadNamespaceDuplicatePrefix() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                // Can't define same namespace prefix more than once in the same scope.
                wax.Start("root")
                   .Namespace("tns", "http://www.ociweb.com/tns")
                   .Namespace("tns", "http://www.ociweb.com/tns")
                   .Close();

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("The namespace prefix \"tns\" is already in scope.", ex.Message);
            }
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadNamespaceMultipleDefault() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                // Can't define default namespace more than once in the same scope.
                wax.Start("root")
                   .Namespace("http://www.ociweb.com/tns1")
                   .Namespace("http://www.ociweb.com/tns2")
                   .Close();

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("The namespace prefix \"\" is already in scope.", ex.Message);
            }
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testBadPrefix() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Start("root");
                wax.Start("parent");
                wax.Namespace("foo", "http://www.ociweb.com/foo");
                wax.Child("foo", "child1", "one");
                wax.End();
                wax.Child("foo", "child2", "two");
                wax.Close();

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("The namespace prefix \"foo\" isn't in scope.", ex.Message);
            }
        }

        [Fact] //(expected=IllegalStateException.class)
        public void testBadText() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Text("text"); // haven't called start yet

                Assert.True(false, "Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
                Assert.Equal("can't call text when state is IN_PROLOG", ex.Message);
            }
        }

        [Fact] //(expected=RuntimeException.class)
        public void testBadWrite() /* throws IOException */ {
            try {
                string filePath = TEMP_XML_FILE_PATH;
                FileStream fs = File.OpenWrite(filePath);
                WAX wax = new WAX(fs);
                wax.Start("root");
                fs.Close(); // closed the Writer instead of allowing WAX to do it
                File.Delete(filePath);
                wax.Close(); // attempting to write more after the Writer was closed

                Assert.True(false, "Expecting IOException (in Exception)");
            } catch (Exception ex) {
                Assert.Equal("Cannot access a closed file.", ex.Message);
            }
        }

        [Fact] //(expected=RuntimeException.class)
        public void testBadWriteFile() /* throws IOException */ {
            try {
                string filePath = "."; // the current directory, not a file
                new WAX(filePath);

                Assert.True(false, "Expecting IOException (in Exception)");
            }
            catch (Exception ex)
            {
                // TODO: Fix this test, and its assertion:
                Assert.True(ex.Message.StartsWith("Access to the path '"));
                Assert.True(ex.Message.EndsWith("' is denied."));
            }
        }

        [Fact] //(expected=RuntimeException.class)
        public void testBadXSLTAfterRoot() /* throws IOException */ {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Start("root");
                wax.Xslt("root.xslt"); // can't write pi after root element

                Assert.True(false, "Expecting IOException (in Exception)");
            }
            catch (Exception ex)
            {
                Assert.Equal("can't call xslt when state is IN_START_TAG", ex.Message);
            }
        }

        [Fact]
        public void testBig() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.Start("root");
            wax.NlText("text #1");
            wax.Child("child1", "text");
            wax.NlText("text #2");
            wax.Start("child2").Attr("a1", "v1").End();
            wax.NlText("text #3");
            wax.Close();

            string xml = "<root>\n" + "  text #1\n" + "  <child1>text</child1>\n"
                + "  text #2\n" + "  <child2 a1=\"v1\"/>\n" + "  text #3\n"
                + "</root>";

            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testBlankLine() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.Start("root").BlankLine().Close();

            string xml =
                "<root>\n" +
                "\n" +
                "</root>";
            Assert.Equal(xml, sw.ToString());
        }
        
        [Fact]
        public void testCDATA() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.Start("root").CData("1<2>3&4'5\"6").Close();

            string xml =
                "<root>\n" +
                "  <![CDATA[1<2>3&4'5\"6]]>\n" +
                "</root>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testComment() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.Comment("comment #1").Comment("comment #2")
               .Start("root").Comment("comment #3").Close();

            string xml =
                "<!-- comment #1 -->\n" +
                "<!-- comment #2 -->\n" +
                "<root>\n" +
                "  <!-- comment #3 -->\n" +
                "</root>";

            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testDTD() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.Dtd("http://www.ociweb.com/xml/root.dtd");
            wax.Start("root");
            wax.Close();

            string xml =
                "<!DOCTYPE root SYSTEM \"http://www.ociweb.com/xml/root.dtd\">\n" +
                "<root/>";

            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testEmpty() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.Start("root").Close();

            Assert.Equal("<root/>", sw.ToString());
        }

        [Fact]
        public void testEntityDef() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.EntityDef("name", "value").Start("root").Close();

            string xml = "<!DOCTYPE root [<!ENTITY name \"value\">]><root/>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testExternalEntityDef() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.ExternalEntityDef("name", "value").Start("root").Close();

            string xml = "<!DOCTYPE root [<!ENTITY name SYSTEM \"value\">]><root/>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testEscape() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.Start("root").Text("abc<def>ghi'jkl\"mno&pqr").Close();

            string xml =
                "<root>abc&lt;def&gt;ghi&apos;jkl&quot;mno&amp;pqr</root>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact] //(expected=IllegalStateException.class)
        public void testExtraEnd() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.SetIndent(null);
                wax.Start("root").End().End().Close();

                Assert.True(false, "Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
                Assert.Equal("can't call end when state is AFTER_ROOT", ex.Message);
            }
        }

        [Fact]
        public void testGetIndent() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            Assert.Equal("  ", wax.GetIndent());

            wax.SetIndent("    ");
            Assert.Equal("    ", wax.GetIndent());

            wax.SetIndent("\t");
            Assert.Equal("\t", wax.GetIndent());

            wax.SetIndent(null);
            Assert.Equal(null, wax.GetIndent());

            wax.SetIndent("");
            Assert.Equal("", wax.GetIndent());
        }

        [Fact]
        public void testIndentByNum() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(2);
            wax.Start("root").Child("child", "text").Close();

            string xml = "<root>\n" + "  <child>text</child>\n" + "</root>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testIndentBystring() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent("  ");
            wax.Start("root").Child("child", "text").Close();

            string xml = "<root>\n" + "  <child>text</child>\n" + "</root>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testnamespace_() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.Start("root")
               .Namespace("http://www.ociweb.com/tns1")
               .Namespace("tns2", "http://www.ociweb.com/tns2")
               .Namespace("tns3", "http://www.ociweb.com/tns3")
               .Close();

            string xml = "<root" +
                " xmlns=\"http://www.ociweb.com/tns1\"" +
                " xmlns:tns2=\"http://www.ociweb.com/tns2\"" +
                " xmlns:tns3=\"http://www.ociweb.com/tns3\"/>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testNoArgCtor() {
            TextWriter oldConsoleOut = Console.Out;
            StringWriter sw = new StringWriter();
            Console.SetOut(sw);
            try
            {
                WAX wax = new WAX();
                wax.Start("root").Close();
                Assert.Equal("<root/>", sw.ToString());
            }
            finally
            {
                Console.SetOut(oldConsoleOut);
            }
        }

        [Fact]
        public void testNewInstanceNoArg()
        {
            TextWriter oldConsoleOut = Console.Out;
            StringWriter sw = new StringWriter();
            Console.SetOut(sw);
            try
            {
                WAX.NewInstance().Start("root").Close();
                Assert.Equal("<root/>", sw.ToString());
            }
            finally
            {
                Console.SetOut(oldConsoleOut);
            }
        }

        [Fact]
        public void testNoIndent() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.Start("root").Child("child", "text").Close();

            string xml = "<root><child>text</child></root>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact] //(expected=IllegalStateException.class)
        public void testNoRoot() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.Close();

                Assert.True(false, "Expected InvalidOperationException.");
            }
            catch (InvalidOperationException ex)
            {
                Assert.Equal("can't call close when state is IN_PROLOG", ex.Message);
            }
        }

        [Fact]
        public void testProcessingInstructionInPrologue() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.ProcessingInstruction("xml-stylesheet",
                "type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"");
            wax.Start("root");
            wax.Close();

            string xml =
                "<?xml-stylesheet type=\"text/xsl\" href=\"http://www.ociweb.com/foo.xslt\"?>\n" +
                "<root/>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testProcessingInstructionAfterPrologue() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.Start("root");
            wax.ProcessingInstruction("target", "data");
            wax.Close();

            string xml = "<root><?target data?></root>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testPrefix() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.Start("foo", "root")
               .Attr("bar", "baz")
               // Note that the namespace is defined after it is used,
               // but on the same element, which should be allowed.
               .Namespace("foo", "http://www.ociweb.com/foo")
               .Close();

            string xml = "<foo:root bar=\"baz\" " +
                "xmlns:foo=\"http://www.ociweb.com/foo\"/>";
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testSchemasWithIndent() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.Start("root")
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
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testSchemasWithoutIndent() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.Start("root")
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
            Assert.Equal(xml, sw.ToString());
        }

        [Fact]
        public void testText() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetIndent(null);
            wax.Start("root").Text("text").Close();

            Assert.Equal("<root>text</root>", sw.ToString());
        }

        [Fact] //(expected=IllegalArgumentException.class)
        public void testTrustMeFalse() {
            try {
                StringWriter sw = new StringWriter();
                WAX wax = new WAX(sw);
                wax.SetTrustMe(false);
                // Since error checking is turned on,
                // element names must be valid and text is escaped.
                wax.Start("123").Text("<>&'\"").Close();

                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("\"123\" is an invalid NMTOKEN", ex.Message);
            }
        }

        [Fact]
        public void testTrustMeTrue() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw);
            wax.SetTrustMe(true);
            wax.SetIndent(null);
            // Since error checking is turned off,
            // invalid element names and unescaped text are allowed.
            wax.Start("123").Text("<>&'\"").Close();

            Assert.Equal("<123><>&'\"</123>", sw.ToString());
        }

        [Fact]
        public void testWriteFile() /* throws IOException */ {
            string filePath = TEMP_XML_FILE_PATH;
            WAX wax = new WAX(filePath);
            wax.SetIndent(null);
            wax.Start("root").Text("text").Close();
            Assert.Equal("<root>text</root>", getFileFirstLine(filePath));
            File.Delete(filePath);
        }

        [Fact]
        public void testNewInstanceFile() /* throws IOException */ {
            string filePath = TEMP_XML_FILE_PATH;
            WAX wax = WAX.NewInstance(filePath) as WAX;
            wax.SetIndent(null);
            wax.Start("root").Text("text").Close();
            Assert.Equal("<root>text</root>", getFileFirstLine(filePath));
            File.Delete(filePath);
        }

        [Fact]
        public void testWriteStream() /* throws IOException */ {
            MemoryStream ms = new MemoryStream();
            WAX wax = new WAX(ms);
            wax.SetIndent(null);
            wax.Start("root").Text("text").Close();
            Assert.Equal("<root>text</root>", ToString(ms));
        }

        [Fact]
        public void testNewInstanceStream() /* throws IOException */ {
            MemoryStream ms = new MemoryStream();
            WAX wax = WAX.NewInstance(ms) as WAX;
            wax.SetIndent(null);
            wax.Start("root").Text("text").Close();
            Assert.Equal("<root>text</root>", ToString(ms));
        }

        [Fact]
        public void testNewInstanceWriter() /* throws IOException */ {
            StringWriter sw = new StringWriter();
            WAX wax = WAX.NewInstance(sw) as WAX;
            wax.SetIndent(null);
            wax.Start("root").Text("text").Close();
            Assert.Equal("<root>text</root>", sw.ToString());
        }

        [Fact]
        public void testXMLDeclaration() {
            StringWriter sw = new StringWriter();
            WAX wax = new WAX(sw, WAX.Version.V1_0);
            wax.SetIndent(null);
            wax.Start("root").Close();

            string xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root/>";
            Assert.Equal(xml, sw.ToString());
        }

        // TODO: Tests for 'NewInstance' methods.
        // TODO: Test for 'TerminateStart' method.

        private string getFileFirstLine(string filePath) /* throws IOException */ {
            Stream inputStream = File.OpenRead(filePath);
            StreamReader streamReader = new StreamReader(inputStream);
            string line = streamReader.ReadLine();
            streamReader.Close();
            return line;
        }

        private static string ToString(MemoryStream ms)
        {
            return new StreamReader(new MemoryStream(ms.ToArray())).ReadToEnd();
        }

    }
}
