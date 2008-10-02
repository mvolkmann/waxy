package com.ociweb.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ociweb.xml.gargoylesoftware.EqualsTester;

public class CharRangeTest {

    @Test
    public void testToString() {
        assertEquals("[a-z]", new CharRange('a', 'z').toString());
        assertEquals("[0-9]", new CharRange('0', '9').toString());
        assertEquals("[x]", new CharRange('x').toString());
    }

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

    @Test
    public void testOneCharEqualsRangeOfOneChar() {
        final CharRange single = new CharRange('x');
        final CharRange range = new CharRange('x', 'x');
        assertEquals(single, range);
        assertEquals(range, single);
    }

    @Test
    public void testComparable() {

        final CharRange A = new CharRange('A');
        final CharRange AtoF = new CharRange('A', 'F');
        final CharRange AtoZ = new CharRange('A', 'Z');
        final CharRange B = new CharRange('B');
        final CharRange BtoZ = new CharRange('B', 'Z');
        final CharRange Z = new CharRange('Z');

        assertComparableOrder(A, AtoF, AtoZ, B, BtoZ, Z);
    }

    private void assertComparableOrder(final CharRange... charRanges) {
        for (int firstIndex = 0; firstIndex < charRanges.length; firstIndex++) {
            final CharRange firstRange = charRanges[firstIndex];
            final Comparable<CharRange> firstComparable = firstRange;

            assertEquals("Expecting " + firstComparable + " to equal itself.", //
                    0, firstComparable.compareTo(firstRange));

            for (int secondIndex = firstIndex + 1; secondIndex < charRanges.length; secondIndex++) {
                final CharRange secondRange = charRanges[secondIndex];
                final Comparable<CharRange> secondComparable = secondRange;

                {
                    final int firstComparedToSecond = firstComparable
                            .compareTo(secondRange);
                    assertTrue("Expecting " + firstRange + " < " + secondRange
                            + " -- result = " + firstComparedToSecond,
                            firstComparedToSecond < 0);
                }

                {
                    final int secondCompareToFirst = secondComparable
                            .compareTo(firstRange);
                    assertTrue("Expecting " + firstRange + " > " + secondRange
                            + " -- result = " + secondCompareToFirst,
                            secondCompareToFirst > 0);
                }
            }
        }
    }

}
