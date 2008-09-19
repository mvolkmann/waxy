package com.ociweb.xml;

/**
 * This interface defines the methods that can be called
 * after the beginning of a start tag has been output, but
 * before it has been terminated with an end tag or the shorthand way (/>).
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
public interface ElementWAX extends CommonWAX {

    /**
     * @see WAX#blankLine()
     */
    ElementWAX blankLine();

    /**
     * @see WAX#cdata(String)
     */
    ElementWAX cdata(String text);

    /**
     * @see WAX#cdata(String, boolean)
     */
    ElementWAX cdata(String text, boolean newLine);

    /**
     * @see WAX#child(String, String)
     */
    ElementWAX child(String name, String text);

    /**
     * @see WAX#child(String, String, String)
     */
    ElementWAX child(String prefix, String name, String text);

    /**
     * @see WAX#close()
     */
    void close();

    /**
     * @see WAX#end()
     */
    ElementWAX end();

    /**
     * @see WAX#text(String)
     */
    ElementWAX text(String text);

    /**
     * @see WAX#text(String, boolean, boolean)
     */
    ElementWAX text(String text, boolean newLine);
}