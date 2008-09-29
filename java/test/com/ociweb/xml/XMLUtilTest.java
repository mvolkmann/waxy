package com.ociweb.xml;

import static org.junit.Assert.*;

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

    @Test(expected=IllegalArgumentException.class)
    public void testBadComment() {
        XMLUtil.verifyComment("one -- two");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBadName() {
        XMLUtil.verifyName("1a");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testBadVersion() {
        XMLUtil.verifyVersion("1.3");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadURI() {
        XMLUtil.verifyURI(":junk");
    }

    @Test
    public void testEscape() {
        assertEquals("&lt;", XMLUtil.escape("<"));
        assertEquals("&gt;", XMLUtil.escape(">"));
        assertEquals("&amp;", XMLUtil.escape("&"));
        assertEquals("&apos;", XMLUtil.escape("'"));
        assertEquals("&quot;", XMLUtil.escape("\""));
        assertEquals("1&lt;2&gt;3&amp;4&apos;5&quot;6", XMLUtil.escape("1<2>3&4'5\"6"));
    }

    @Test
    public void testIsComment() {
        assertTrue(XMLUtil.isComment("one two"));
        assertTrue(XMLUtil.isComment("one - two"));
        assertTrue(!XMLUtil.isComment("one -- two"));
        assertTrue(!XMLUtil.isComment("-- one two"));
        assertTrue(!XMLUtil.isComment("one two --"));
    }

    @Test
    public void testIsNameOld() {
        assertTrue(!XMLUtil.isName(null));

        assertTrue(XMLUtil.isName("a1"));
        assertTrue(XMLUtil.isName("_a1"));
        assertTrue(!XMLUtil.isName("1a"));

        // Name tokens cannot begin with "XML" in any case.
        assertTrue(!XMLUtil.isName("xmlFoo"));
        assertTrue(!XMLUtil.isName("XMLFoo"));
        assertTrue(!XMLUtil.isName("xMLFoo"));

        // Try some non-Latin Unicode characters.
        String name = "\u3105\u0F20";
        assertTrue(XMLUtil.isName(name));
    }

    @Test
    public void testIsURI() {
        assertTrue(XMLUtil.isURI("http://www.ociweb.com/foo"));
        assertTrue(!XMLUtil.isURI(":junk"));
    }

    @Test
    public void testIsVersion() {
        assertTrue(XMLUtil.isVersion("1.0"));
        assertTrue(XMLUtil.isVersion("1.1"));
        assertTrue(XMLUtil.isVersion("1.2"));
        assertTrue(!XMLUtil.isVersion("1.3"));
    }

	@Test
	public void testIsName() {
		shouldNotBeValidName(null);

		shouldBeValidName("a1");
		shouldNotBeValidName("1a");

		// Name tokens cannot begin with "XML" in any case.
		shouldNotBeValidName("xmlFoo");
		shouldNotBeValidName("XMLFoo");
		shouldNotBeValidName("xMLFoo");

		shouldBeValidName("\u3105\u0F20");
	}

	protected static void checkElementName(final StringBuffer errs,
			final boolean expectIsValid, final String name) {

		final String postfix = expectIsValid //
		? " should be considered a valid XML Element name, but it is not." //
				: " should NOT be considered a valid XML Element name, but it is.";

		final String message = "<" + quote(name) + ">" + postfix;

		if (XMLUtil.isName(name) != expectIsValid)
			errs.append(message).append('\n');
	}

	private static String quote(final String string) {

		if (string == null)
			return "null";

		final StringBuffer sb = new StringBuffer(string.length());
		for (int idx = 0; idx < string.length(); ++idx) {
			final char c = string.charAt(idx);
			if (c > ' ' && c < 127)
				sb.append(c);
			else
				sb.append("[" + Integer.toHexString(c) + "]");
		}
		return sb.toString();
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
		final String message = "<" + quote(name)
				+ "> should be considered a valid XML Element name.";
		assertTrue(message, XMLUtil.isName(name));
	}

	private static void shouldNotBeValidName(final String name) {
		final String message = "<" + quote(name)
				+ "> should NOT be considered a valid XML Element name.";
		assertFalse(message, XMLUtil.isName(name));
	}
}