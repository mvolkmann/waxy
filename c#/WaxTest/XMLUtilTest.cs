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
                XMLUtil.verifyComment("one -- two");
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
                XMLUtil.verifyNMToken("1a");
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
                XMLUtil.verifyVersion("1.3");
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
                XMLUtil.verifyURI(":junk");
                Assert.Fail("Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.AreEqual("\":junk\" is an invalid URI", ex.Message);
            }
        }

        [Test]
        public void testEscape() {
            Assert.AreEqual("&lt;", XMLUtil.escape("<"));
            Assert.AreEqual("&gt;", XMLUtil.escape(">"));
            Assert.AreEqual("&amp;", XMLUtil.escape("&"));
            Assert.AreEqual("&apos;", XMLUtil.escape("'"));
            Assert.AreEqual("&quot;", XMLUtil.escape("\""));
            Assert.AreEqual("1&lt;2&gt;3&amp;4&apos;5&quot;6", XMLUtil.escape("1<2>3&4'5\"6"));
        }

        [Test]
        public void testIsComment() {
            Assert.IsTrue(XMLUtil.isComment("one two"));
            Assert.IsTrue(XMLUtil.isComment("one - two"));
            Assert.IsTrue(!XMLUtil.isComment("one -- two"));
            Assert.IsTrue(!XMLUtil.isComment("-- one two"));
            Assert.IsTrue(!XMLUtil.isComment("one two --"));
        }

        [Test]
        public void testIsNMToken() {
            Assert.IsTrue(XMLUtil.isNMToken("a1"));
            Assert.IsTrue(!XMLUtil.isNMToken("1a"));
        }

        [Test]
        public void testIsURI() {
            Assert.IsTrue(XMLUtil.isURI("http://www.ociweb.com/foo"));
            Assert.IsTrue(!XMLUtil.isURI(":junk"));
        }

        [Test]
        public void testIsVersion() {
            Assert.IsTrue(XMLUtil.isVersion("1.0"));
            Assert.IsTrue(XMLUtil.isVersion("1.1"));
            Assert.IsTrue(XMLUtil.isVersion("1.2"));
            Assert.IsTrue(!XMLUtil.isVersion("1.3"));
        }
    }
}
