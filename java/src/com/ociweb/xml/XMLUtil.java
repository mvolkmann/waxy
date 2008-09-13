package com.ociweb.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * This class provides utility methods for working with XML.
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
public class XMLUtil {

    /**
     * The default encoding used in XML declarations.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The regular expression used to determine whether a given string
     * is a valid XML "name token" (required for element and attribute names).
     */
    public static final Pattern NMTOKEN_PATTERN =
        Pattern.compile("[A-Za-z][A-Za-z0-9\\-_\\.]*");

    public static final String XMLSCHEMA_INSTANCE_NS =
        "http://www.w3.org/1999/XMLSchema-instance";

    /**
     * Creating instances of this class is not allowed
     * since all methods are static.
     */
    private XMLUtil() {}

    /**
     * Escapes special characters in XML text.
     * @param text the original text
     * @return the escaped text
     */
    public static String escape(String text) {
        // Escape special characters in text.
        StringBuffer sb = new StringBuffer();
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
            } else {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }

    /**
     * Determines whether given text is a valid comment.
     * @param text the text
     */
    public static boolean isComment(String text) {
        return !text.contains("--");
    }

    /**
     * Determines whether given text is a name token.
     * @param text the text
     * @return true if a name token; false otherwise
     */
    public static boolean isNMToken(String text) {
        if (text == null) return false;

        // Names that start with "XML" in any case are reserved.
        if (text.toLowerCase().startsWith("xml")) return false;

        return NMTOKEN_PATTERN.matcher(text).matches();
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
     * @param text the text
     * @throws IllegalArgumentException if it isn't valid
     */
    public static void verifyComment(String text) {
        if (!isComment(text)) {
            throw new IllegalArgumentException(
                '"' + text + "\" is an invalid comment");
        }
    }

    /**
     * Verifies that the given text is a valid name token.
     * @param text the text
     * @throws IllegalArgumentException if it isn't valid
     */
    public static void verifyNMToken(String text) {
        if (!isNMToken(text)) {
            throw new IllegalArgumentException(
                '"' + text + "\" is an invalid NMTOKEN");
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
