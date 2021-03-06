package com.ociweb.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * This class provides utility methods for working with XML.
 * 
 * <p>
 *   Copyright (c) 2008, R. Mark Volkmann<br />
 *   All rights reserved.
 * </p>
 * <p>
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * </p>
 * <ul>
 *   <li>
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   </li>
 *   <li>
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   </li>
 *   <li>
 *     Neither the name of Object Computing, Inc. nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *   </li>
 * </ul>
 * <p>
 *   This software is provided by the copyright holders and contributors "as is"
 *   and any express or implied warranties, including, but not limited to,
 *   the implied warranties of merchantability and fitness for a particular
 *   purpose are disclaimed. In no event shall the copyright owner or
 *   contributors be liable for any direct, indirect, incidental, special,
 *   exemplary, or consequential damages (including, but not limited to,
 *   procurement of substitute goods or services; loss of use, data, or profits;
 *   or business interruption) however caused and on any theory of liability,
 *   whether in contract, strict liability, or tort (including negligence
 *   or otherwise) arising in any way out of the use of this software,
 *   even if advised of the possibility of such damage.
 * </p>
 * @author R. Mark Volkmann, Object Computing, Inc.
 */
public final class XMLUtil {

    /**
     * The default encoding used in XML declarations.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /*
     * From http://www.w3.org/TR/2006/REC-xml11-20060816/#charsets
     * 
     * Names and Tokens
     * 
     * [4] NameStartChar ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] |
     * [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] | [#x37F-#x1FFF] |
     * [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] |
     * [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
     * 
     * [4a] NameChar ::= NameStartChar | "-" | "." | [0-9] | #xB7 |
     * [#x0300-#x036F] | [#x203F-#x2040]
     * 
     * Note: The following regular expression implementations of the XML naming
     * standards, these changes have been made: 1. The colon character (":") is
     * not allowed in WAX names because namespace prefixes are managed directly
     * through the WAX API method parameters. 2. "[#x10000-#xEFFFF]" is excluded
     * because Java 5 offers only 16-bit Unicode support. IE: Only the
     * "Basic Multilingual Plane (BMP)".
     */
    private static final String NAME_START_CHAR_CLASS_RANGES = ""
        + "A-Z" + "_" + "a-z"
        + "\u00C0-\u00D6" + "\u00D8-\u00F6" + "\u00F8-\u02FF"
        + "\u0370-\u037D" + "\u037F-\u1FFF" + "\u200C-\u200D"
        + "\u2070-\u218F" + "\u2C00-\u2FEF" + "\u3001-\uD7FF"
        + "\uF900-\uFDCF" + "\uFDF0-\uFFFD";

    private static final String NAME_CHAR_CLASS_RANGES = ""
        + "-" // Must be first character of character class regex.
        + NAME_START_CHAR_CLASS_RANGES
        + Pattern.quote(".") + "0-9"
        + "\u00B7" + "\u0300-\u036F" + "\u203F-\u2040";

    /**
     * Element and attribute names must be name tokens.
     * This is a regular expression used to determine whether a given string
     * is a valid XML "name token" using any valid Unicode characters.
     */
    public static final Pattern FULL_NAME_PATTERN = Pattern.compile(
        "^[" + NAME_START_CHAR_CLASS_RANGES + "]["
        + NAME_CHAR_CLASS_RANGES + "]*$");

    //public static final String XMLSCHEMA_INSTANCE_NS =
    //    "http://www.w3.org/1999/XMLSchema-instance";

    /**
     * Creating instances of this class is not allowed
     * since all methods are static.
     */
    private XMLUtil() {
    }

    /**
     * Escapes special characters in XML text.
     * @param value an Object whose toString value is to be escaped
     * @return the escaped text
     */
    public static String escape(Object value) {
        if (value == null) return "";

        String text = value.toString();

        // Escape special characters in text.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '\n') {
                sb.append("&#xA;");
            } else if (c == '\t') {
                sb.append("&#x9;");
            } else {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }

    /**
     * @param value
     * @return <code>true</code> if the given <code>String value</code> has a
     *         non-<code>null</code> non-empty value.<br/>
     *         <code>false</code> if <code>null</code> or empty.
     */
    public static boolean hasValue(final String value) {
        return (value != null) && (value.length() > 0);
    }

    /**
     * Determines whether given text is a valid comment.
     * 
     * @param text the text
     * @throws IllegalArgumentException
     *             if <code>text</code> is <code>null</code> or contains two
     *             sequential dash characters (<code>"--"</code>).
     */
    public static boolean isComment(String text) {
        return (text != null) && !text.contains("--");
    }

    /**
     * Determines whether given text is a name token.
     * @param text the text
     * @return true if a name token; false otherwise
     */
    public static boolean isName(String text) {
        if (text == null) return false;

        // Names that start with "XML" in any case are reserved.
        if (text.toLowerCase().startsWith("xml")) return false;

        // Since that didn't match, try the full regular expression.
        return FULL_NAME_PATTERN.matcher(text).matches();
    }

    /**
     * Determines whether given text is a URI.
     * @param text the text
     * @return true if a URI; false otherwise
     */
    public static boolean isURI(String text) {
        try {
            new URI(text);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Determines whether given text is a valid XML version.
     * @param text the text
     * @return true if a valid version; false otherwise
     */
    public static boolean isVersion(String text) {
        return "1.0".equals(text) || "1.1".equals(text) || "1.2".equals(text);
    }

    /**
     * Verifies that the given text is a valid comment.
     *
     * @param text the text
     * @throws IllegalArgumentException
     *             if <code>text</code> is <code>null</code> or contains two
     *             sequential dash characters (<code>"--"</code>).
     */
    public static void verifyComment(String text) {
        if (!isComment(text)) {
            throw new IllegalArgumentException(
                '"' + text + "\" is an invalid comment");
        }
    }

    /**
     * Verifies that the given text is a valid name token.
     *
     * @param text the text
     * @throws IllegalArgumentException
     *             if <code>text</code> is not a valid XML name token.
     */
    public static void verifyName(String text) {
        if (!isName(text)) {
            throw new IllegalArgumentException(
                '"' + text + "\" is an invalid XML name");
        }
    }

    /**
     * Verifies that the given text is a valid URI.
     * @param text the text
     * @throws IllegalArgumentException if it isn't valid
     */
    public static void verifyURI(String text) {
        if (!isURI(text)) {
            throw new IllegalArgumentException(
                '"' + text + "\" is an invalid URI");
        }
    }
    
    /**
     * Verifies that the given text is a valid XML version.
     * @param text the text
     * @throws IllegalArgumentException if it isn't valid
     */
    public static void verifyVersion(String text) {
        if (!isVersion(text)) {
            throw new IllegalArgumentException(
                '"' + text + "\" is an invalid XML version");
        }
    }
}
