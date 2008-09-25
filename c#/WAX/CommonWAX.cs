namespace WAXNamespace
{
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
    public interface CommonWAX
    {

        /**
         * @see WAX#Comment(string)
         */
        PrologOrElementWAX Comment(string text);

        /**
         * @see WAX#ProcessingInstruction(string, string)
         */
        PrologOrElementWAX ProcessingInstruction(string target, string data);

        /**
         * @see WAX#Start(string)
         */
        StartTagWAX Start(string name);

        /**
         * @see WAX#Start(string, string)
         */
        StartTagWAX Start(string prefix, string name);
    }
}
