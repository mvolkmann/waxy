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
package com.ociweb.xml.gargoylesoftware;


import java.util.Date;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 *  Tests for EqualsTester
 *
 * @version  $Revision: 1.8 $
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 */
public class EqualsTesterTest extends TestCase {
    /**
     *  Create an instance
     *
     * @param  name Name of the test
     */
    public EqualsTesterTest( final String name ) {
        super( name );
    }


    /**
     *  Test the good case
     */
    public void testProperlyImplementedClass() {
        final ProperlyImplementedEquals a = new ProperlyImplementedEquals( 200 );
        final ProperlyImplementedEquals b = new ProperlyImplementedEquals( 200 );
        final ProperlyImplementedEquals c = new ProperlyImplementedEquals( 400 );
        final ProperlyImplementedEquals d =
            new ProperlyImplementedEquals( 200 ) {
            };

        new EqualsTester( a, b, c, d );
    }


    /**
     *  Test various combinations of null parameters
     */
    public void testEquals_NullValues() {
        final Date a = new Date( 200 );
        final Date b = new Date( 200 );
        final Date c = new Date( 400 );
        final Date d = new java.sql.Date( 200 );

        try {
            new EqualsTester( null, b, c, d );
            fail( "Expected failure for null A" );
        }
        catch( final AssertionFailedError expected ) {
            // Expected path
        }

        try {
            new EqualsTester( a, null, c, d );
            fail( "Expected failure for null B" );
        }
        catch( final AssertionFailedError expected ) {
            // Expected path
        }

        try {
            new EqualsTester( a, b, null, d );
            fail( "Expected failure for null C" );
        }
        catch( final AssertionFailedError expected ) {
            // Expected path
        }
    }


    /**
     *  Test passing in null when the class we are testing is final. This should
     *  be legal
     */
    public void testNullDForFinalClass() {
        final EqualChecker equalChecker =
            new EqualChecker() {
                public boolean equals( final TestObject object1, final TestObject object2 ) {
                    return object2 != null && object1.value_ == object2.value_;
                }
            };
        final TestObject a = new TestObject( equalChecker, 200 );
        final TestObject b = new TestObject( equalChecker, 200 );
        final TestObject c = new TestObject( equalChecker, 400 );
        final TestObject d = null;

        try {
            new EqualsTester( a, b, c, d );
        }
        catch( final NullPointerException e ) {
            fail( "EqualsTester should allow null for D when class is final" );
        }
    }


    /**
     * Test passing in a null D for a non-final class
     */
    public void testNullDForNonFinalClass() {
        /** Fake class for testing */
        class Foo {
            private boolean state;

            /** @param b The state */
            public Foo( final boolean b ) {
                state = b;
            }

            /**
             * @param object The object to compare against
             * @return true if the objects are equal 
             */
            public boolean equals( final Object object ) {
                if( object != null && object.getClass() == this.getClass() ) {
                    return state == ( (Foo)object ).state;
                }
                return false;
            }
            
            /** @return the hash code */
            public int hashCode() {
                return 2;
            }
        }

        final Foo a = new Foo( true );
        final Foo b = new Foo( true );
        final Foo c = new Foo( false );
        final Foo d = null;

        try {
            new EqualsTester( a, b, c, d );
            fail( "EqualsTester should not allow null for D when class is not final" );
        }
        catch( final DetailedNullPointerException e ) {
            assertEquals("D", e.getArgumentName());
        }
    }


    /**
     *  Test passing in a subclass that has a badly implemented equals method
     */
    public void testSubclassWithBadEquals() {
        final Date a = new Date( 200 );
        final Date b = new Date( 200 );
        final Date c = new Date( 400 );
        final Date d =
            new Date( 200 ) {
                private static final long serialVersionUID = 1L;
				public boolean equals( final Object object ) {
                    return true;
                }
                public int hashCode() {
                    return 2;
                }
            };

        try {
            new EqualsTester( a, b, c, d );
            fail( "Class and subclass cannot be equal" );
        }
        catch( final AssertionFailedError e ) {
            assertEquals( "a.equals(d)", e.getMessage() );
        }
    }


    /**
     *  Test a.equals(a)
     */
    public void testEquals_AequalsA() {
        final EqualChecker equalChecker =
            new EqualChecker() {
                public boolean equals( final TestObject object1, final TestObject object2 ) {
                    if( object1 == object2 ) {
                        return false;
                    }
                    else {
                        return object1.value_ == object2.value_;
                    }
                }
            };

        final TestObject a = new TestObject( equalChecker, 200 );
        final TestObject b = new TestObject( equalChecker, 200 );
        final TestObject c = new TestObject( equalChecker, 400 );
        final TestObject d = null;
        try {
            new EqualsTester( a, b, c, d );
            fail( "A.equals(A)" );
        }
        catch( final AssertionFailedError e ) {
            // Expected path
        }
    }


    private static final class TestObject {
        private final EqualChecker equalChecker_;
        private final int value_;


        /**
         *  Create an instance
         *
         * @param  value
         * @param  equalChecker
         */
        public TestObject( final EqualChecker equalChecker, final int value ) {
            equalChecker_ = equalChecker;
            value_ = value;
        }

        /** @inheritDoc Object#equals(Object) */
        public boolean equals( final Object object ) {
            return equalChecker_.equals( this, (TestObject)object );
        }

        /** @inheritDoc Object#hashCode() */
        public int hashCode() {
            return value_;
        }
    }


    private static class EqualChecker {
    		/**
    		 * Compare these two objects for equality
    		 * @param object1 The first object
    		 * @param object2 The second Object
    		 * @return True if they are equal.
    		 */
        public boolean equals( final TestObject object1, final TestObject object2 ) {
            return object1 == object2;
        }
    }


    private class ProperlyImplementedEquals {
        private final int value_;

        /** @param value Some value */
        public ProperlyImplementedEquals( final int value ) {
            value_ = value;
        }

        /** @inheritDoc Object#equals(Object) */
        public boolean equals( final Object object ) {
            if( object != null && this.getClass() == object.getClass() ) {
                final ProperlyImplementedEquals pie = (ProperlyImplementedEquals)object;
                return value_ == pie.value_;
            }
            return false;
        }

        /** @inheritDoc Object#hashCode() */
        public int hashCode() {
            return value_;
        }
    }
}

