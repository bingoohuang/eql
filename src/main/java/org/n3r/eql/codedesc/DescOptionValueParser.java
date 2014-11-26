package org.n3r.eql.codedesc;

import org.n3r.eql.parser.OffsetAndOptionValue;

public class DescOptionValueParser {
    public OffsetAndOptionValue parseValueOption(String valueStr) {
        int size = valueStr.length();
        int start = 0;
        for (; start < size; ++start) {
            char aChar = valueStr.charAt(start);
            if (',' == aChar) {
                ++start;
                break;
            }
            if (!Character.isSpaceChar(aChar)) break;
        }

        int atPos = valueStr.indexOf('@', start);
        if (atPos <= 0) return null;

        int offset = atPos + 1;

        for (; offset < size; ++offset) {
            char ch = valueStr.charAt(offset);
            if (!Character.isSpaceChar(ch)) break;
        }

        boolean brace = false;
        for (; offset < size; ++offset) {
            char ch = valueStr.charAt(offset);
            if (Character.isSpaceChar(ch)) break;
            if (',' == ch) break;
            if ('(' == ch) {
                ++offset;
                brace = true;
                break;
            }
        }

        if (brace) {
            for (; offset < size; ++offset) {
                if (')' == valueStr.charAt(offset)) {
                    ++offset;
                    break;
                }
            }
        }


        return new OffsetAndOptionValue(offset, valueStr.substring(start, offset).trim());
    }
}
