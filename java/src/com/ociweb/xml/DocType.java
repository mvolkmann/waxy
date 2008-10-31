package com.ociweb.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold DTD information, including "<code>&lt;!ENTITY ... &gt;</code>"
 * declarations, and write them to the output when needed. (IE: Write just
 * before the root XML Element is written.) This class exists to hold related
 * information together, and to enable discarding this information, freeing
 * memory, when it is no longer needed.
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
 * @author Jeff Grigg
 */
/* package */ class DocType {

    private String publicId;
    private String systemId;
    private final List<String> entityDefs = new ArrayList<String>();

    public DocType(final String publicId, final String systemId) {
        this.publicId = publicId;
        this.systemId = systemId;
    }

    public void write(final XMLWriter out, final String rootElementName) {
        out.writeDocType(rootElementName, publicId, systemId,
                entityDefs);
    }

    public void entityDef(final String name, final String value) {
        entityDefs.add(name + " \"" + value + '"');
    }

}
