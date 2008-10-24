package com.ociweb.xml;

/**
 * This interface defines the methods that can be called
 * after the beginning of a start tag has been output, but it
 * (the start tag itself, not a terminating end tag) has been closed.
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
public interface StartTagWAX extends ElementWAX {

    /**
     * @see WAX#attr(String, Object)
     */
    StartTagWAX attr(String name, Object value);

    /**
     * @see WAX#attr(String, String, Object)
     */
    StartTagWAX attr(String prefix, String name, Object value);

    /**
     * @see WAX#attr(String, String, Object, boolean)
     */
    StartTagWAX attr(
        String prefix, String name, Object value, boolean newLine);

    /**
     * @see WAX#defaultNamespace(String)
     */
    StartTagWAX defaultNamespace(String uri);

    /**
     * @see WAX#defaultNamespace(String, String)
     */
    StartTagWAX defaultNamespace(String uri, String schemaPath);

    /**
     * @see WAX#defaultNS(String)
     */
    StartTagWAX defaultNS(String uri);

    /**
     * @see WAX#defaultNS(String, String)
     */
    StartTagWAX defaultNS(String uri, String schemaPath);

    /**
     * @see WAX#namespace(String, String)
     */
    StartTagWAX namespace(String prefix, String uri);

    /**
     * @see WAX#namespace(String, String, String)
     */
    StartTagWAX namespace(String prefix, String uri, String schemaPath);

    /**
     * @see WAX#ns(String, String)
     */
    StartTagWAX ns(String prefix, String uri);

    /**
     * @see WAX#ns(String, String, String)
     */
    StartTagWAX ns(String prefix, String uri, String schemaPath);

    /**
     * @see WAX#unescapedAttr(String, Object)
     */
    StartTagWAX unescapedAttr(String name, Object value);

    /**
     * @see WAX#unescapedAttr(String, String, Object)
     */
    StartTagWAX unescapedAttr(String prefix, String name, Object value);

    /**
     * @see WAX#unescapedAttr(String, String, Object, boolean)
     */
    StartTagWAX unescapedAttr(
        String prefix, String name, Object value, boolean newLine);
}
