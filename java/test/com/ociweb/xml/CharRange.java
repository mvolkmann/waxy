package com.ociweb.xml;

public class CharRange implements Comparable<CharRange> {

    public final char start;
    public final char end;

    public CharRange(final char singleCharacter) {
        this(singleCharacter, singleCharacter);
    }

    public CharRange(final char start, final char end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        if (start == end)
            return "[" + start + "]";
        else
            return "[" + start + '-' + end + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final CharRange that = (CharRange) obj;
        return (this.start == that.start) && (this.end == that.end);
    }

    @Override
    public int hashCode() {
        return start * 31 + end;
    }

	// Java 6: @Override
    public int compareTo(final CharRange obj) {
        final CharRange that = (CharRange) obj;
        final int startDiff = this.start - that.start;
        if (startDiff != 0) return startDiff;
        return this.end - that.end;
    }
}
