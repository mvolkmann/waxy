using System;
using System.Collections.Generic;
using System.Text;

namespace WAXNamespace
{
    /**
     * This interface defines the methods that can be called
     * after the beginning of a start tag has been output, but it
     * (the start tag itself, not a terminating end tag) has been closed.
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
    public interface StartTagWAX : ElementWAX {

        /**
         * @see WAX#attr(string, Object)
         */
        StartTagWAX attr(string name, Object value);

        /**
         * @see WAX#attr(string, string, Object)
         */
        StartTagWAX attr(string prefix, string name, Object value);

        /**
         * @see WAX#attr(bool, string, string, Object)
         */
        StartTagWAX attr(
            bool newLine, string prefix, string name, Object value);

        /**
         * @see WAX#Namespace(string)
         */
        StartTagWAX Namespace(string uri);

        /**
         * @see WAX#Namespace(string, string)
         */
        StartTagWAX Namespace(string prefix, string uri);

        /**
         * @see WAX#Namespace(string, string, string)
         */
        StartTagWAX Namespace(string prefix, string uri, string schemaPath);
    }
}
