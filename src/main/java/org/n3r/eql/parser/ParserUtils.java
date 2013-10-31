package org.n3r.eql.parser;

import java.util.regex.Pattern;

public class ParserUtils {
    public static Pattern inlineComment = Pattern.compile("/\\*\\s*(.+?)\\s*\\*/");
    public static String substr(String line, int startIndex) {
        if (startIndex >= line.length()) return "";

        return line.substring(startIndex).trim();
    }

    public static String trim(String expr) {
        return expr != null ? expr.trim() : null;
    }

    public static boolean isBlank(String collection) {
        return collection == null || collection.trim().length() == 0;
    }
}
