package com.ociweb.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharRangeUtil {

    private static final String BEGIN = "^";
    private static final String END = "$";

    protected static CharRange[] toCharRanges(
            final String xmlStandardCharacterRanges) {

        final List<CharRange> charRanges = new ArrayList<CharRange>();

        final ICharRangeRecognizer oneCharacterRecognizer =
            new OneCharacterRecognizer();
        final ICharRangeRecognizer mulipleCharacterRangeRecognizer =
            new MultipleCharacterRangeRecognizer();

        final String[] characterRangeSpecifications =
            xmlStandardCharacterRanges.split(Pattern.quote("|") + "|" + ",");
        for (int specIdx = 0;
             specIdx < characterRangeSpecifications.length;
             ++specIdx) {
            final String rangeSpec =
                characterRangeSpecifications[specIdx].trim();

            final CharRange oneCharacterRange =
                oneCharacterRecognizer.recognize(rangeSpec);
            final CharRange multipleCharacterRange =
                mulipleCharacterRangeRecognizer.recognize(rangeSpec);

            if (oneCharacterRange != null) {
                charRanges.add(oneCharacterRange);
            } else if (multipleCharacterRange != null) {
                charRanges.add(multipleCharacterRange);
            } else {
                throw createIllegalArgumentException(specIdx, rangeSpec);
            }
        }

        final CharRange[] charRangesAsArray = new CharRange[charRanges.size()];
        charRanges.toArray(charRangesAsArray);
        return charRangesAsArray;
    }

    private static char toChar(final String charSpec) {
        if (charSpec.length() == 1) {
            return charSpec.charAt(0);
        } else if (charSpec.startsWith("#x")) {
            return (char) Integer.parseInt(charSpec.substring(2), 16);
        } else {
            throw new IllegalArgumentException(
                    "Not a valid character specification:  <" + charSpec + ">");
        }
    }

    private static String capturingGroup(final String regex) {
        return "(" + regex + ")";
    }

    private static String nonCapturingGroup(final String regex) {
        return "(?:" + regex + ")";
    }

    private static String quote(final char c) {
        return Pattern.quote(String.valueOf(c));
    }

    private static IllegalArgumentException createIllegalArgumentException(
            final int charIndex, final String rangeSpec) {

        final String message = "Argument index <" + charIndex
                + "> is not a character or range of characters:  <" + rangeSpec
                + ">";
        return new IllegalArgumentException(message);
    }

    private static class OneCharacterRecognizer
    implements ICharRangeRecognizer {

        private final Pattern pattern = Pattern.compile(""
                + BEGIN
                + capturingGroup(""
                        + nonCapturingGroup("'.'")
                        + "|"
                        + nonCapturingGroup("#x[0-9A-F]{2,4}")
                )
                + END
        );

        public CharRange recognize(final String rangeSpec) {
            final Matcher matcher = pattern.matcher(rangeSpec);

            if (matcher.matches()) {
                final String singleCharSpec = matcher.group(1);
                if (singleCharSpec.startsWith("'")
                        && singleCharSpec.endsWith("'")) {
                    final char matchedCharacter = singleCharSpec.charAt(1);
                    return new CharRange(matchedCharacter);
                } else if (singleCharSpec.startsWith("#x")) {
                    final char matchedCharacter = (char) Integer.parseInt(
                            singleCharSpec.substring(2), 16);
                    return new CharRange(matchedCharacter);
                } else {
                    throw new InternalError(
                            "Cannot process this valid single character spec:  <"
                                    + singleCharSpec + ">");
                }
            } else {
                return null;
            }
        }
    }

    private static class MultipleCharacterRangeRecognizer
    implements ICharRangeRecognizer {

        private static final String singleCharacterSpecRegEx =
            capturingGroup(".|" + nonCapturingGroup("#x[0-9A-F]{2,4}"));

        private static final Pattern pattern = Pattern.compile(""
                + BEGIN
                + quote('[')
                + singleCharacterSpecRegEx + "-" + singleCharacterSpecRegEx
                + quote(']')
                + END
        );

        public CharRange recognize(final String rangeSpec) {
            final Matcher matcher = pattern.matcher(rangeSpec);

            if (matcher.matches()) {
                final String startRangeSpec = matcher.group(1);
                final String endRangeSpec = matcher.group(2);

                final char startChar = toChar(startRangeSpec);
                final char endChar = toChar(endRangeSpec);
                
                return new CharRange(startChar, endChar);
            } else {
                return null;
            }
        }
    }

    private interface ICharRangeRecognizer {
        CharRange recognize(String rangeSpec);
    }
}
