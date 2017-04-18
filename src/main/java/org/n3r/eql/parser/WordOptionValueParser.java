package org.n3r.eql.parser;

public class WordOptionValueParser implements OptionValueParser {
    public static OptionValueParser instance = new WordOptionValueParser();

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public OffsetAndOptionValue parseValueOption(String valueStr) {
        char[] chars = valueStr.toCharArray();
        int offset = 0;
        for (; offset < chars.length; ++offset) {
            char aChar = chars[offset];
            if (!Character.isSpaceChar(aChar)) break;
        }

        boolean quoted = offset < chars.length
                && (chars[offset] == '\'' || chars[offset] == '"');
        char quoteChar = quoted ? chars[offset] : '\0';
        if (quoted) ++offset;

        StringBuilder optionValue = new StringBuilder(chars.length - offset);
        for (; offset < chars.length; ++offset) {
            if (quoted) {
                if (chars[offset] == quoteChar) {
                    ++offset;
                    break;
                }
                optionValue.append(chars[offset]);
            } else {
                if (Character.isSpaceChar(chars[offset])) break;
                optionValue.append(chars[offset]);
            }
        }

        return new OffsetAndOptionValue(offset, optionValue.toString());
    }
}
