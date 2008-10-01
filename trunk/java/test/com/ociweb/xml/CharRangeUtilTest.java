package com.ociweb.xml;

import static com.ociweb.xml.CharRangeUtil.toCharRanges;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class CharRangeUtilTest {

    //
    // TODO:
    // From:
    // http://www.w3.org/TR/2006/REC-xml11-20060816/#charsets
    //
    // [4] NameStartChar ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] |
    // [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] | [#x37F-#x1FFF] |
    // [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] |
    // [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
    //
    // [4a] NameChar ::= NameStartChar | "-" | "." | [0-9] | #xB7 |
    // [#x0300-#x036F] | [#x203F-#x2040]
    //
    // ...which translates to...
    //
    // final String xmlNameStartChar =
    // "':' | [A-Z] | '_' | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]"
    // ;
    // final String xmlNameChar = xmlNameStartChar +
    // " | '-' | '.' | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]";
    //

    @Test
    public void testOneChar() {
        assertArrayEquals(new CharRange[] { new CharRange('x') },
                toCharRanges("'x'"));
    }

    @Test
    public void testSingleHexChar() {
        assertArrayEquals(new CharRange[] { new CharRange('\u00B7') },
                toCharRanges("#xB7"));
    }

    @Test
    public void testOneRange() {
        assertArrayEquals(new CharRange[] { new CharRange('a', 'z') },
                toCharRanges("[a-z]"));
    }

    @Test
    public void testIgnoreSpacesAroundSingleChar() {
        assertArrayEquals(new CharRange[] { new CharRange('.') },
                toCharRanges("   '.'   "));
    }

    @Test
    public void testIgnoreSpacesAroundCharRange() {
        assertArrayEquals(new CharRange[] { new CharRange('0', '9') },
                toCharRanges("   [0-9]   "));
    }

    @Test
    public void testRangeOfTwoDigitUnicodeCharacters() {
        assertArrayEquals(
                new CharRange[] { new CharRange('\u00C0', '\u00D6') },
                toCharRanges("[#xC0-#xD6]"));
    }

    @Test
    public void testRangeOfThreeDigitUnicodeCharacters() {
        assertArrayEquals(
                new CharRange[] { new CharRange('\u0370', '\u037D') },
                toCharRanges("[#x370-#x37D]"));
    }

    @Test
    public void testRangeOfFourDigitUnicodeCharacters() {
        assertArrayEquals(
                new CharRange[] { new CharRange('\u200C', '\u200D') },
                toCharRanges("[#x200C-#x200D]"));
    }

    @Test
    public void testMultipleCharacterRanges() {
        final CharRange[] expectedCharRanges = new CharRange[] {
                new CharRange('-'), new CharRange('0', '9'),
                new CharRange('\u00B7'), new CharRange('\u0300', '\u036F') };
        assertArrayEquals(expectedCharRanges,
                toCharRanges("'-' | [0-9] | #xB7 | [#x0300-#x036F]"));
    }

    @Test
    public void testEmptyStringError() {
        try {
            toCharRanges("");

            fail("Expected IllegalArgumentException.");
        } catch (final IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals(
                    "Argument index <0> is not a character or range of characters:  <>",
                    expectedIllegalArgumentException.getMessage());
        }
    }

    @Test
    public void testBadCharacterRangeSpec() {
        try {
            toCharRanges("'-' | [0-9] | bad | [a-z] | '?'");

            fail("Expected IllegalArgumentException.");
        } catch (final IllegalArgumentException expectedIllegalArgumentException) {
            assertEquals(
                    "Argument index <2> is not a character or range of characters:  <bad>",
                    expectedIllegalArgumentException.getMessage());
        }
    }
}
