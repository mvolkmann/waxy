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
    public void testIsName() {
        assertTrue(!XMLUtil.isName(null));

        assertTrue(XMLUtil.isName("a1"));
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

    @Test(expected=IllegalArgumentException.class)
    public void testVerifyVersion() {
        XMLUtil.verifyVersion("2.0");
    }
}