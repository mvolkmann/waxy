package com.ociweb.xml;

/**
 * This interface groups methods that are shared with
 * PrologueWAX and ElementWAX.
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
public interface CommonWAX {

    /**
     * @see WAX#comment(String)
     */
    PrologOrElementWAX comment(String text);

    /**
     * @see WAX#comment(String, boolean)
     */
    PrologOrElementWAX comment(String text, boolean newLine);

    /**
     * @see WAX#processingInstruction(String, String)
     */
    PrologOrElementWAX processingInstruction(String target, String data);

    /**
     * @see WAX#setEscape(boolean)
     */
    StartTagWAX setEscape(boolean escape);

    /**
     * @see WAX#start(String)
     */
    StartTagWAX start(String name);

    /**
     * @see WAX#start(String, String)
     */
    StartTagWAX start(String prefix, String name);
}