package com.ociweb.xml;

/**
 * This enum specifies the version of XML being used.
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
public enum Version {

    /**
     * Disables writing of the "<?xml version=... ?>" specification)
     */
    UNSPECIFIED(null),

    /**
     * Either
     * <a href="http://www.w3.org/TR/2006/REC-xml-20060816/">XML 1.0, Forth Edition</a>
     * or
     * <a href="http://www.w3.org/TR/2008/PER-xml-20080205/">XML 1.0, Fifth Edition</a>
     */
    V1_0("1.0"),

    /**
     * <a href="http://www.w3.org/TR/2006/REC-xml11-20060816/">XML 1.1, Second Edition</a>
     */
    V1_1("1.1"),

    /**
     * There is currently no standard document for "XML 1.2".
     * <p>
     * <i>(Nor is there likey to be one in the forseeable future -- given the
     * current pragmatic approach illustrated by "XML 1.0, Fith Edition"
     * changes.)</i>
     * </p>
     */
    V1_2("1.2");


    private final String versionNumberString;

    private Version(final String versionNumberString) {
        this.versionNumberString = versionNumberString;
    }

    public String getVersionNumberString() {
        return versionNumberString;
    }
}
