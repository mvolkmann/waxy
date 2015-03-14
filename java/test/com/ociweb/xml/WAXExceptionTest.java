package com.ociweb.xml;

import static org.junit.Assert.*;

import java.lang.reflect.Modifier;
import org.junit.Test;

public class WAXExceptionTest {

    @Test
    public void testClassShouldBeAbstract() {
        assertTrue("Expecting WAXException class to be abstract.",
                Modifier.isAbstract(WAXException.class.getModifiers()));
    }

    @Test
    public void testConstructorAcceptsThrowable() {
        final WAXException waxException = new WAXException("message", new Throwable()) {
            private static final long serialVersionUID = 1L;
        };
        assertNotNull(waxException);
    }
}
