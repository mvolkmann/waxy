/*
 * Copyright (c) 2002, 2004 Gargoyle Software Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment:
 *
 *       "This product includes software developed by Gargoyle Software Inc.
 *        (http://www.GargoyleSoftware.com/)."
 *
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 4. The name "Gargoyle Software" must not be used to endorse or promote
 *    products derived from this software without prior written permission.
 *    For written permission, please contact info@GargoyleSoftware.com.
 * 5. Products derived from this software may not be called "GSBase", nor may
 *    "GSBase" appear in their name, without prior written permission of
 *    Gargoyle Software Inc.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GARGOYLE
 * SOFTWARE INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ociweb.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import junit.framework.Assert;


/**
 * EqualsTester is used to test the equals contract on objects. The contract as
 * specified by java.lang.Object states that if A.equals(B) is true then
 * B.equals(A) is also true. It also specifies that if A.equals(B) is true then
 * A.hashCode() will equals B.hashCode().
 * <p>
 *
 * It is also common practice to implement equals using an instanceof check
 * which will result in false positives in some cases. Specifically, it will
 * result in false positives when comparing against a subclass with the same
 * values. For an in-depth discussion of the common problems when implementing
 * the equals contract, refer to the book "Practical Java" by Peter Haggar
 *
 * <pre>
 * // WRONG way of implementing equals
 * public boolean equals( final Object object ) {
 *     if( object instanceof this ) {
 *        // do check
 *     }
 *     return false;
 * }
 * </pre>
 *
 * The correct way to implement equals is as follows
 *
 * <pre>
 * public boolean equals(final Object object) {
 * 	if (object != null &amp;&amp; object.getClass() == this.getClass()) {
 * 		// do check
 * 	}
 * 	return false;
 * }
 * </pre>
 *
 * EqualsTester ensures that the equals() and hashCode() methods have been
 * implemented correctly.
 * <p>
 *
 * <pre>
 * final Object a = new Foo(4); // original object
 * final Object b = new Foo(4); // another object that has the same values as the original
 * final Object c = new Foo(5); // another object with different values
 * final Object d = new Foo(4) {
 * }; // a subclass of Foo with the same values as the original
 * new EqualsTester(a, b, c, d);
 * </pre>
 *
 * @version $Revision: 1.4 $
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 */
public class EqualsTester extends Assert {

	/**
	 * Perform the test. The act of instantiating one of these will run the
	 * test.
	 *
	 * @param a
	 *            The object to be tested
	 * @param b
	 *            An object that is equal to A
	 * @param c
	 *            An object of the same class that is not equal to A. If it is
	 *            not possible to create a different one then pass null.
	 * @param d
	 *            A subclass of A with the same values. If A is an instance of a
	 *            final class then this must be null
	 */
	public EqualsTester(final Object a, final Object b, final Object c,
			final Object d) {
		assertNotNull(a, "A");
		assertNotNull(b, "B");
		assertSameClassAsA(a, b, "B");

		if (c == null) {
			assertCAllowedToBeNull(a.getClass());
		} else {
			assertSameClassAsA(a, c, "C");
		}

		if (isClassFinal(a.getClass())) {
			assertNull(d, "D");
		} else if (d == null) {
			throw new DetailedNullPointerException("D",
					"Cannot be null for a non-final class");
		}

		if (d != null) {
			assertDDifferentClassThanA(a, d);
		}

		assertAEqualsNull(a);
		assertAEqualsA(a);
		assertAEqualsB(a, b);
		if (c != null) {
			assertANotEqualC(a, c);
		}
		assertClassAndSubclass(a, d);
	}

	private boolean isClassFinal(final Class<?> clazz) {
		final int modifiers = clazz.getModifiers();
		return Modifier.isFinal(modifiers);
	}

	private void assertAEqualsA(final Object a) {
		assertTrue("A.equals(A)", a.equals(a));
	}

	private void assertAEqualsB(final Object a, final Object b) {
		assertTrue("A.equals(B)", a.equals(b));
		assertTrue("B.equals(A)", b.equals(a));
		assertEquals("hashCode", a.hashCode(), b.hashCode());
	}

	private void assertANotEqualC(final Object a, final Object c) {
		assertTrue("a.equals(c)", a.equals(c) == false);
		assertTrue("c.equals(a)", c.equals(a) == false);
	}

	private void assertClassAndSubclass(final Object a, final Object d) {
		if (d != null) {
			if (a.equals(d) == true) {
				fail("a.equals(d)");
			}

			if (d.equals(a) == true) {
				fail("d.equals(a)");
			}
		}
	}

	private void assertNotNull(final Object object, final String description) {
		if (object == null) {
			fail(description + " is null");
		}
	}

	private void assertNull(final Object object, final String description) {
		if (object != null) {
			fail(description + " must be null.  Found [" + object + "]");
		}
	}

	/**
	 * C may not be null if it has a public non-default constructor or any
	 * setXX() methods
	 *
	 * @param clazz
	 */
	private void assertCAllowedToBeNull(final Class<?> clazz) {
		int i;

		final Constructor<?> constructors[] = clazz.getConstructors();
		for (i = 0; i < constructors.length; i++) {
			if (constructors[i].getParameterTypes().length != 0) {
				fail("C may not be null because it has a public non-default constructor: "
						+ constructors[i]);
			}
		}

		final Method methods[] = clazz.getMethods();
		for (i = 0; i < methods.length; i++) {
			if (methods[i].getName().startsWith("set")) {
				fail("C may not be null because it has public set methods: "
						+ methods[i]);
			}
		}
	}

	private void assertSameClassAsA(final Object a, final Object object,
			final String name) {
		if (a.getClass() != object.getClass()) {
			fail(name + " must be of same class as A.  A.class=["
					+ a.getClass().getName() + "] " + name + ".class=["
					+ object.getClass().getName() + "]");
		}
	}

	private void assertDDifferentClassThanA(final Object a, final Object d) {
		if (a.getClass() == d.getClass()) {
			fail("D must not be of same class as A.  A.class=["
					+ a.getClass().getName() + "] d.class=["
					+ d.getClass().getName() + "]");
		}
	}

	private void assertAEqualsNull(final Object a) {
		try {
			if (a.equals(getNullObjectValue()) == true) {
				fail("A.equals(null) returned true");
			}
		} catch (final NullPointerException e) {
			fail("a.equals(null) threw a NullPointerException.  It should have returned false");
		}
	}

	/**
     * This method "hides" the null from FindBugs -- a tool that doesn't like
     * <code>.equals(null)</code> calls.
     *
     * @return <code>null</code> -- <i>in <b>all</b> cases.</i>
     */
    private static Object getNullObjectValue() {
        return null;
    }
}
