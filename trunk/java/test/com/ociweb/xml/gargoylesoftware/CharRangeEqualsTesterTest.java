package com.ociweb.xml.gargoylesoftware;

import org.junit.Test;

import com.ociweb.xml.CharRange;

public class CharRangeEqualsTesterTest {

    @Test
    public void testEqualsDiffStart() {
        final CharRange a = new CharRange('a', 'z');
        final CharRange b = new CharRange('a', 'z');
        final CharRange c = new CharRange('x', 'z');
        final CharRange d = new CharRange('a', 'z') {
        };

        new EqualsTester(a, b, c, d);
    }

    @Test
    public void testEqualsDiffEnd() {
        final CharRange a = new CharRange('a', 'z');
        final CharRange b = new CharRange('a', 'z');
        final CharRange c = new CharRange('a', 'x');
        final CharRange d = new CharRange('a', 'z') {
        };

        new EqualsTester(a, b, c, d);
    }

}
