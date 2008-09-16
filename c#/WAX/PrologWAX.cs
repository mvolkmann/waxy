using System;
using System.Collections.Generic;
using System.Text;

namespace WAXNamespace
{
    /**
     * This interface defines the methods that can be called
     * while writing the prologue section of an XML document.
     * 
     * <p>Copyright 2008 R. Mark Volkmann</p>
     * <p>This file is part of WAX.</p>
     * <p>
     * WAX is free software.  You can redistribute it and/or modify it
     * under the terms of the GNU Lesser General Public License as published
     * by the Free Software Foundation, either version 3 of the License,
     * or (at your option) any later version.
     * </p>
     * <p>
     * WAX is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty
     * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
     * See the GNU Lesser General Public License for more details.
     * </p>
     * <p>
     * You should have received a copy of the GNU Lesser General Public License
     * along with WAX.  If not, see http://www.gnu.org/licenses.
     * </p>
     * 
     * @author R. Mark Volkmann, Object Computing, Inc.
     */
    public interface PrologWAX : CommonWAX {

        /**
         * @see WAX#dtd(string)
         */
        PrologWAX dtd(string filePath);

        /**
         * @see WAX#entityDef(string, string)
         */
        PrologWAX entityDef(string name, string value);

        /**
         * @see WAX#externalEntityDef(string, string)
         */
        PrologWAX externalEntityDef(string name, string filePath);

        PrologWAX xslt(string filePath);
    }
}
