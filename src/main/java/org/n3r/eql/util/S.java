package org.n3r.eql.util;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S {
    public static String escapeSingleQuotes(String stringValue) {
        return stringValue.replaceAll("'", "''");
    }

    public static String cleanQuote(String option) {
        if (option == null) return "";

        String ret = option;
        if (option.startsWith("\"")) ret = ret.substring(1);
        if (option.endsWith("\"")) ret = ret.substring(0, ret.length() - 1);

        return ret;
    }


    public static boolean isBlank(String string) {
        return string == null || string.length() == 0 || string.trim().length() == 0;
    }

    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }


    public static String trimRight(String original) {
        return original == null ? "" : original.replaceAll("\\s+$", "");
    }


    public static String trimLeft(String original) {
        return original == null ? "" : original.replaceAll("^\\s+", "");
    }


    public static String substrInQuotes(String str, char left, int pos) {
        int leftTimes = 0;
        int leftPos = str.indexOf(left, pos);
        if (leftPos < 0) return "";

        for (int i = leftPos + 1; i < str.length(); ++i) {
            char charAt = str.charAt(i);
            if (charAt == left) ++leftTimes;
            else if (matches(left, charAt)) {
                if (leftTimes == 0) return str.substring(leftPos + 1, i);
                --leftTimes;
            }
        }

        return "";
    }

    // return true if 'left' and 'right' are matching parens/brackets/braces
    public static boolean matches(char left, char right) {
        if (left == '(') return right == ')';
        if (left == '[') return right == ']';
        if (left == '{') return right == '}';
        return false;
    }


    public static final Pattern INTEGER_PATTERN = Pattern.compile("[-+]?([0-9]+)$");

    /**
     * 判断字符串是否整数。
     *
     * @param string 字符串。
     * @return true 是整数。
     */
    public static boolean isInteger(String string) {
        if (Strings.isNullOrEmpty(string)) {
            return false;
        }

        Matcher matcher = INTEGER_PATTERN.matcher(string);
        if (!matcher.matches()) {
            return false;
        }

        String number = matcher.group(1);
        String maxValue = "" + Integer.MAX_VALUE;
        if (number.length() > maxValue.length()) {
            return false;
        }

        return alignRight(number, maxValue.length(), '0').compareTo(maxValue) <= 0;
    }

    /**
     * 在字符串左侧填充一定数量的特殊字符.
     *
     * @param cs    字符串
     * @param width 字符数量
     * @param c     字符
     * @return 新字符串
     */
    public static String alignRight(CharSequence cs, int width, char c) {
        if (null == cs) return null;
        int len = cs.length();
        if (len >= width) return cs.toString();
        return repeat(c, width - len) + cs;
    }

    private static String repeat(char ch, int times) {
        return Strings.repeat("" + ch, times);
    }


    public static boolean equalsIgnoreCase(String s1, String s2) {
        return s1 != null ? s1.equalsIgnoreCase(s2) : s2 == null;
    }

    public static boolean containsIgnoreCase(String string, String value) {
        return string == null ? false : string.toUpperCase().contains(value.toUpperCase());
    }


    public static int indexOfBlank(CharSequence cs) {
        int sz = cs.length();
        for (int i = 0; i < sz; i++)
            if (Character.isWhitespace(cs.charAt(i))) return i;

        return -1;
    }

    public static boolean endsWith(String str, String end) {
        return str != null ? str.endsWith(end) : false;
    }

    public static String upperCase(String str) {
        return str != null ? str.toUpperCase() : null;
    }


    public static String trim(String s) {
        return s != null ? s.trim() : null;
    }

    public static String bytesToStr(byte[] bytes) {
        return new String(bytes, Charsets.UTF_8);
    }

    public static byte[] toBytes(String value) {
        return value.getBytes(Charsets.UTF_8);
    }

    public static boolean parseBool(String str) {
        return "true".equalsIgnoreCase(str)
                || "yes".equalsIgnoreCase(str)
                || "on".equalsIgnoreCase(str);
    }

    /**
     * 缺省的日期时间显示格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static String toDateTimeStr(Timestamp date) {
        return new SimpleDateFormat(DEFAULT_DATETIME_FORMAT).format(date);
    }


    public static boolean isNotEmpty(String s) {
        return s != null && s.length() > 0;
    }
}
