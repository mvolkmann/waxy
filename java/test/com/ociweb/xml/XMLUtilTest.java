package com.ociweb.xml;

import static com.ociweb.xml.CharRangeUtil.toCharRanges;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

/**
 * This class provides unit test methods for XMLUtil.
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
public class XMLUtilTest {

    // See: http://www.w3.org/TR/2006/REC-xml11-20060816/#charsets
    // XML 1.2 NameStartChar:
    // Excluded because we handle namespaces ourselves in WAX: "':' | " +
    private static final String XML12_NAME_START_CHAR = "[A-Z] | '_' | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD]";
    // Not valid in Java 5: + "| [#x10000-#xEFFFF]";

    // XML 1.2 NameChar:
    private static final String XML12_NAME_CHAR = XML12_NAME_START_CHAR
            + " | '-' | '.' | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]";

    @Test(expected=IllegalArgumentException.class)
    public void testBadComment() {
        XMLUtil.verifyComment("one -- two");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullComment() {
        XMLUtil.verifyComment(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadName() {
        XMLUtil.verifyName("1a");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBadURI() {
        XMLUtil.verifyURI(":junk");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testVerifyVersion_BadVersion() {
        XMLUtil.verifyVersion("1.3");
    }

    @Test
    public void testVerifyVersion_GoodVersions() {
        XMLUtil.verifyVersion("1.0");
        XMLUtil.verifyVersion("1.1");
        XMLUtil.verifyVersion("1.2");
    }

    @Test
    public void testEscape() {
        assertEquals("&lt;", XMLUtil.escape("<"));
        assertEquals("&gt;", XMLUtil.escape(">"));
        assertEquals("&amp;", XMLUtil.escape("&"));
        assertEquals("&apos;", XMLUtil.escape("'"));
        assertEquals("&quot;", XMLUtil.escape("\""));
        assertEquals("1&lt;2&gt;3&amp;4&apos;5&quot;6",
            XMLUtil.escape("1<2>3&4'5\"6"));
    }

    @Test
    public void testIsComment() {
        assertTrue(XMLUtil.isComment("one two"));
        assertTrue(XMLUtil.isComment("one - two"));
        assertFalse(XMLUtil.isComment("one -- two"));
        assertFalse(XMLUtil.isComment("-- one two"));
        assertFalse(XMLUtil.isComment("one two --"));
    }

    @Test
    public void testIsURI() {
        assertTrue(XMLUtil.isURI("http://www.ociweb.com/foo"));
        assertFalse(XMLUtil.isURI(":junk"));
    }

    @Test
    public void testIsVersion() {
        assertTrue(XMLUtil.isVersion("1.0"));
        assertTrue(XMLUtil.isVersion("1.1"));
        assertTrue(XMLUtil.isVersion("1.2"));
        assertFalse(XMLUtil.isVersion("1.3"));
    }

    @Test
    public void testIsName_Null() {
        shouldNotBeValidName(null);
    }

    @Test
    public void testIsName_GoodNames() {
        shouldBeValidName("a1");
        shouldNotBeValidName("1a");
    }

    @Test
    public void testIsName_XMLPrefixes() {
        // Name tokens cannot begin with "XML" in any case.
        shouldNotBeValidName("xmlFoo");
        shouldNotBeValidName("XMLFoo");
        shouldNotBeValidName("xMLFoo");
    }

    @Test
    public void testIsName_UnicodeCharacters() {
        shouldBeValidName("\u3105\u0F20");
    }

    /**
     * Not a name because we handle namespace prefixes explicitly in the WAX API
     * methods.
     */
    @Test
    public void testIsName_WithNamespaceColon() {
        shouldNotBeValidName("ns:name");
    }

    @Test
    public void testIsName_LeadingUnderscore() {
        shouldBeValidName("_LeadingUnderscore");
    }

    @Test
    public void testIsName_LeadingQuotedUnderscore() {
        shouldNotBeValidName("'_'yz");
    }

    @Test
    public void testIsName_NonLeadingQuotedDot() {
        shouldNotBeValidName("x'.'z");
    }

    @Test
    public void testIsName_NonLeadingQuotedAnyCharacter() {
        shouldNotBeValidName("x'?'z");
    }

    @Test
    public void testIsName_NonLeadingQuotedDash() {
        shouldNotBeValidName("x'-'z");
    }

    @Test
    public void testIsName_NonLeadingQuotedUnderscore() {
        shouldNotBeValidName("x'_'z");
    }

    @Test
    public void testIsName_NonLeadingQuotedColon() {
        shouldNotBeValidName("x':'z");
    }

    private static void shouldBeValidName(final String name) {
        final String message = "<" + renderSpecialCharactersVisible(name)
                + "> should be considered a valid XML Element name.";
        assertTrue(message, XMLUtil.isName(name));
    }

    private static void shouldNotBeValidName(final String name) {
        final String message = "<" + renderSpecialCharactersVisible(name)
                + "> should NOT be considered a valid XML Element name.";
        assertFalse(message, XMLUtil.isName(name));
    }

    @Test
    public void testUnicodeXmlNameStartCharacter() {
        validateNamesWithCharacterRanges(XML12_NAME_START_CHAR,
                new IGenerateName() {
                    // Java 6: @Override
                    public String nameContainingChar(final char chr) {
                        return chr + "__";
                    }
                });
    }

    @Test
    public void testUnicodeXmlOtherNameCharacters() {
        validateNamesWithCharacterRanges(XML12_NAME_CHAR, new IGenerateName() {
            // Java 6: @Override
            public String nameContainingChar(final char chr) {
                return "_" + chr + "_";
            }
        });
    }

    private static void validateNamesWithCharacterRanges(
            final String definedCharacterRanges, final IGenerateName callback) {

        final StringBuffer errs = new StringBuffer();

        final CharRange[] xmlNameStartCharRanges = toCharRanges(definedCharacterRanges);
        Arrays.sort(xmlNameStartCharRanges);

        char startSkippedChar = '\0';
        for (int idx = 0; idx < xmlNameStartCharRanges.length; idx++) {
            final CharRange charRange = xmlNameStartCharRanges[idx];

            checkNamesBeforeRange(callback, errs, startSkippedChar, charRange);
            checkNamesInRange(callback, errs, charRange);

            startSkippedChar = (char) (charRange.end + 1);
        }

        checkToEndOfUnicode(callback, errs, startSkippedChar);

        if (errs.length() > 0) {
            final String errorMessageString = errs.toString();
            final int numberOfErrors = errorMessageString.split("\n").length;
            fail(numberOfErrors + " errors:\n" + errorMessageString);
        }
    }

    private static void checkNamesBeforeRange(final IGenerateName callback,
            final StringBuffer errs, char startSkippedChar,
            final CharRange charRange) {

        final char endSkippedChar = (char) (charRange.start - 1);
        final CharRange skipRange = new CharRange(startSkippedChar,
                endSkippedChar);

        for (char chr = skipRange.start; chr <= skipRange.end; ++chr) {
            final String name = callback.nameContainingChar(chr);
            checkElementName(errs, false, name, skipRange);
        }
    }

    private static void checkNamesInRange(final IGenerateName callback,
            final StringBuffer errs, final CharRange charRange) {
        for (char chr = charRange.start; chr <= charRange.end; ++chr) {
            final String name = callback.nameContainingChar(chr);
            checkElementName(errs, true, name, charRange);
        }
    }

    private static void checkToEndOfUnicode(final IGenerateName callback,
            final StringBuffer errs, char startSkippedChar) {
        final CharRange endRange = new CharRange(startSkippedChar, '\uFFFF');
        final char END_OF_RANGE_ROLLOVER_CHAR = 0;
        for (char chr = endRange.start; chr != END_OF_RANGE_ROLLOVER_CHAR; ++chr) {
            final String name = callback.nameContainingChar(chr);
            checkElementName(errs, false, name, endRange);
        }
    }

    private static void checkElementName(final StringBuffer errs,
            final boolean expectIsValid, final String name,
            final CharRange charRange) {

        final String postfix = expectIsValid ? " should be considered a valid XML Element name, but it is not."
                : " should NOT be considered a valid XML Element name, but it is.";

        final String message = "<" + renderSpecialCharactersVisible(name) + ">"
                + postfix + "  in CharRange"
                + renderSpecialCharactersVisible(charRange.toString());

        if (XMLUtil.isName(name) != expectIsValid)
            errs.append(message).append('\n');
    }

    @Test
    public void testRenderSpecialCharactersVisible() {
        assertEquals("", renderSpecialCharactersVisible(""));
        assertEquals("abc", renderSpecialCharactersVisible("abc"));
        assertEquals("\\u0000 \\u001f \\u00ff \\uffff",
                renderSpecialCharactersVisible("\u0000 \u001F \u00FF \uFFFF"));
    }

    private static String renderSpecialCharactersVisible(final String string) {
        if (string == null)
            return "null";

        final StringBuffer sb = new StringBuffer(string.length());
        for (int idx = 0; idx < string.length(); ++idx) {
            final char chr = string.charAt(idx);
            if (chr >= ' ' && chr < 127)
                sb.append(chr);
            else
                sb.append(toJavaHexCharString(chr));
        }
        return sb.toString();
    }

    private static String toJavaHexCharString(final char chr) {
        final String hexString = "0000" + Integer.toHexString(chr);
        return "\\u" + hexString.substring(hexString.length() - 4);
    }

    private interface IGenerateName {
        String nameContainingChar(char chr);
    }
}
