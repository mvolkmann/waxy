using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using System.Text;

namespace WAXNamespace
{
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
    public class XMLUtil
    {
        /**
         * The default encoding used in XML declarations.
         */
        public const string DEFAULT_ENCODING = "UTF-8";

        /**
         * The regular expression used to determine whether a given string
         * is a valid XML "name token" (required for element and attribute names).
         */
        public static Regex NMTOKEN_PATTERN =
            new Regex("^[A-Za-z][A-Za-z0-9\\-_\\.]*$"); // Added ^ & $ -- jtg

        public const string XMLSCHEMA_INSTANCE_NS =
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
        public static string Escape(string text) {
            // Escape special characters in text.
            StringBuilder sb = new StringBuilder();
            // Changed looping: -- jtg
            char[] chars = text.ToCharArray();
            for (int i = 0; i < chars.Length; ++i)
            {
                char c = chars[i];
                if (c == '<') {
                    sb.Append("&lt;");
                } else if (c == '>') {
                    sb.Append("&gt;");
                } else if (c == '\'') {
                    sb.Append("&apos;");
                } else if (c == '"') {
                    sb.Append("&quot;");
                } else if (c == '&') {
                    sb.Append("&amp;");
                } else {
                    sb.Append(c);
                }
            }
            
            return sb.ToString();
        }

        /**
         * Determines whether given text is a valid comment.
         * @param text the text
         */
        public static bool IsComment(string text) {
            return !text.Contains("--");
        }

        /**
         * Determines whether given text is a name token.
         * @param text the text
         * @return true if a name token; false otherwise
         */
        public static bool IsNMToken(string text) {
            if (text == null) return false;
            return NMTOKEN_PATTERN.IsMatch(text);
        }

        /**
         * Determines whether given text is a URI.
         * @param text the text
         * @return true if a URI; false otherwise
         */
        public static bool IsURI(string text) {
            try {
                new Uri(text);
                return true;
            }
            catch (UriFormatException)
            {
                return false;
            }
        }

        /**
         * Determines whether given text is a valid XML version.
         * @param text the text
         * @return true if a valid version; false otherwise
         */
        public static bool IsVersion(string text) {
            return "1.0".Equals(text) || "1.1".Equals(text) || "1.2".Equals(text);
        }

        /**
         * Verifies that the given text is a valid comment.
         * @param text the text
         * @throws ArgumentException if it isn't valid
         */
        public static void VerifyComment(string text) {
            if (!IsComment(text)) {
                throw new ArgumentException(
                    '"' + text + "\" is an invalid comment");
            }
        }

        /**
         * Verifies that the given text is a valid name token.
         * @param text the text
         * @throws ArgumentException if it isn't valid
         */
        public static void VerifyNMToken(string text) {
            if (!IsNMToken(text)) {
                throw new ArgumentException(
                    '"' + text + "\" is an invalid NMTOKEN");
            }
        }

        /**
         * Verifies that the given text is a valid URI.
         * @param text the text
         * @throws ArgumentException if it isn't valid
         */
        public static void VerifyURI(string text) {
            if (!IsURI(text)) {
                throw new ArgumentException(
                    '"' + text + "\" is an invalid URI");
            }
        }
        
        /**
         * Verifies that the given text is a valid XML version.
         * @param text the text
         * @throws ArgumentException if it isn't valid
         */
        public static void VerifyVersion(string text) {
            if (!IsVersion(text)) {
                throw new ArgumentException(
                    '"' + text + "\" is an invalid XML version");
            }
        }
    }
}
