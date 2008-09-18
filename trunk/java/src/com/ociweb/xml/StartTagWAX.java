package com.ociweb.xml;

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
     * @see WAX#attr(boolean, String, String, Object)
     */
    StartTagWAX attr(
        boolean newLine, String prefix, String name, Object value);

    /**
     * @see WAX#commentedStart(String)
     */
    StartTagWAX commentedStart(String name);

    /**
     * @see WAX#commentedStart(String, String)
     */
    StartTagWAX commentedStart(String prefix, String name);

    /**
     * @see WAX#escape(boolean)
     */
    StartTagWAX setEscape(boolean escape);

    /**
     * @see WAX#namespace(String)
     */
    StartTagWAX namespace(String uri);

    /**
     * @see WAX#namespace(String, String)
     */
    StartTagWAX namespace(String prefix, String uri);

    /**
     * @see WAX#namespace(String, String, String)
     */
    StartTagWAX namespace(String prefix, String uri, String schemaPath);
}