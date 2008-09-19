using System;
using WAXNamespace;
using NUnit.Framework;

namespace WaxTest
{
    [TestFixture]
    public class XMLUtilTest
    {
        [Test]
        public void testBadComment()
        {
            try {
                XMLUtil.VerifyComment("one -- two");
                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.AreEqual("\"one -- two\" is an invalid comment", ex.Message);
            }
        }

        [Test]
        public void testBadNMToken()
        {
            try
            {
                XMLUtil.VerifyNMToken("1a");
                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.AreEqual("\"1a\" is an invalid NMTOKEN", ex.Message);
            }
        }

        [Test]
        public void testBadVersion()
        {
            try
            {
                XMLUtil.VerifyVersion("1.3");
                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.AreEqual("\"1.3\" is an invalid XML version", ex.Message);
            }
        }

        [Test]
        public void testBadURI()
        {
            try
            {
                XMLUtil.VerifyURI(":junk");
                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.AreEqual("\":junk\" is an invalid URI", ex.Message);
            }
        }

        [Test]
        public void testEscape() {
            Assert.AreEqual("&lt;", XMLUtil.Escape("<"));
            Assert.AreEqual("&gt;", XMLUtil.Escape(">"));
            Assert.AreEqual("&amp;", XMLUtil.Escape("&"));
            Assert.AreEqual("&apos;", XMLUtil.Escape("'"));
            Assert.AreEqual("&quot;", XMLUtil.Escape("\""));
            Assert.AreEqual("1&lt;2&gt;3&amp;4&apos;5&quot;6", XMLUtil.Escape("1<2>3&4'5\"6"));
        }

        [Test]
        public void testIsComment() {
            Assert.IsTrue(XMLUtil.IsComment("one two"));
            Assert.IsTrue(XMLUtil.IsComment("one - two"));
            Assert.IsTrue(!XMLUtil.IsComment("one -- two"));
            Assert.IsTrue(!XMLUtil.IsComment("-- one two"));
            Assert.IsTrue(!XMLUtil.IsComment("one two --"));
        }

        [Test]
        public void testIsNMToken() {
            Assert.IsTrue(XMLUtil.IsNMToken("a1"));
            Assert.IsTrue(!XMLUtil.IsNMToken("1a"));
        }

        [Test]
        public void testIsURI() {
            Assert.IsTrue(XMLUtil.IsURI("http://www.ociweb.com/foo"));
            Assert.IsTrue(!XMLUtil.IsURI(":junk"));
        }

        [Test]
        public void testIsVersion() {
            Assert.IsTrue(XMLUtil.IsVersion("1.0"));
            Assert.IsTrue(XMLUtil.IsVersion("1.1"));
            Assert.IsTrue(XMLUtil.IsVersion("1.2"));
            Assert.IsTrue(!XMLUtil.IsVersion("1.3"));
        }
    }
}
