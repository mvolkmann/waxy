package com.ociweb.xml;

/**
 * This interface defines the methods that can be called
 * while writing the prologue section of an XML document.
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
public interface PrologWAX extends CommonWAX {

    /**
     * @see WAX#dtd(String)
     */
    PrologWAX dtd(String systemId);

    /**
     * @see WAX#dtd(String, String)
     */
    PrologWAX dtd(String publicId, String systemId);

    /**
     * @see WAX#entityDef(String, String)
     */
    PrologWAX entityDef(String name, String value);

    /**
     * @see WAX#externalEntityDef(String, String)
     */
    PrologWAX externalEntityDef(String name, String filePath);

    /**
     * @see WAX#xslt(String)
     */
    PrologWAX xslt(String filePath);
}
