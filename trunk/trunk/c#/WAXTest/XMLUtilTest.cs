using System;
using WAXNamespace;
using Xunit;

namespace WaxTest
{
    public class XMLUtilTest
    {
        [Fact]
        public void testBadComment()
        {
            try {
                XMLUtil.VerifyComment("one -- two");
                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("\"one -- two\" is an invalid comment", ex.Message);
            }
        }

        [Fact]
        public void testBadNMToken()
        {
            try
            {
                XMLUtil.VerifyNMToken("1a");
                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("\"1a\" is an invalid NMTOKEN", ex.Message);
            }
        }

        [Fact]
        public void testBadVersion()
        {
            try
            {
                XMLUtil.VerifyVersion("1.3");
                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("\"1.3\" is an invalid XML version", ex.Message);
            }
        }

        [Fact]
        public void testBadURI()
        {
            try
            {
                XMLUtil.VerifyURI(":junk");
                Assert.True(false, "Expected ArgumentException.");
            }
            catch (ArgumentException ex)
            {
                Assert.Equal("\":junk\" is an invalid URI", ex.Message);
            }
        }

        [Fact]
        public void testEscape() {
            Assert.Equal("&lt;", XMLUtil.Escape("<"));
            Assert.Equal("&gt;", XMLUtil.Escape(">"));
            Assert.Equal("&amp;", XMLUtil.Escape("&"));
            Assert.Equal("&apos;", XMLUtil.Escape("'"));
            Assert.Equal("&quot;", XMLUtil.Escape("\""));
            Assert.Equal("1&lt;2&gt;3&amp;4&apos;5&quot;6", XMLUtil.Escape("1<2>3&4'5\"6"));
        }

        [Fact]
        public void testIsComment() {
            Assert.True(XMLUtil.IsComment("one two"));
            Assert.True(XMLUtil.IsComment("one - two"));
            Assert.True(!XMLUtil.IsComment("one -- two"));
            Assert.True(!XMLUtil.IsComment("-- one two"));
            Assert.True(!XMLUtil.IsComment("one two --"));
        }

        [Fact]
        public void testIsNMToken() {
            Assert.True(XMLUtil.IsNMToken("a1"));
            Assert.True(!XMLUtil.IsNMToken("1a"));
        }

        [Fact]
        public void testIsURI() {
            Assert.True(XMLUtil.IsURI("http://www.ociweb.com/foo"));
            Assert.True(!XMLUtil.IsURI(":junk"));
        }

        [Fact]
        public void testIsVersion() {
            Assert.True(XMLUtil.IsVersion("1.0"));
            Assert.True(XMLUtil.IsVersion("1.1"));
            Assert.True(XMLUtil.IsVersion("1.2"));
            Assert.True(!XMLUtil.IsVersion("1.3"));
        }
    }
}
