package org.n3r.eql.util;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.common.primitives.Primitives;
import org.n3r.eql.EqlTran;
import org.n3r.eql.map.EqlRun;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EqlUtils {
    private static Pattern FIRST_WORD = Pattern.compile("\\b(\\w+)\\b");

    public static <T> boolean in(T target, T... compares) {
        for (T compare : compares)
            if (Objects.equal(target, compare)) return true;

        return false;
    }

    public static boolean isProcedure(EqlRun.EqlType sqlType) {
        return in(sqlType, EqlRun.EqlType.CALL, EqlRun.EqlType.DECLARE, EqlRun.EqlType.BEGIN);
    }

    public static EqlRun.EqlType parseSqlType(String rawSql) {
        Matcher matcher = FIRST_WORD.matcher(rawSql);
        matcher.find();
        String firstWord = matcher.group(1).toUpperCase();
        return EqlRun.EqlType.valueOf(firstWord);
    }

    public static String getSqlClassPath(int num) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String callerClassName = stackTraceElements[num].getClassName();
        return callerClassName.replace('.', '/') + ".eql";
    }

    private static String trim(String s) {
        return s != null ? s.trim() : null;
    }

    public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) name = resultSetMetaData.getColumnName(columnIndex);
        return name;
    }

    public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
        Object obj = rs.getObject(index);
        String className = null;
        if (obj != null) className = obj.getClass().getName();
        if (obj instanceof Blob) obj = rs.getBytes(index);
        else if (obj instanceof Clob) obj = rs.getString(index);
        else if (className != null &&
                ("oracle.sql.TIMESTAMP".equals(className) ||
                        "oracle.sql.TIMESTAMPTZ".equals(className))) obj = rs.getTimestamp(index);
        else if (className != null && className.startsWith("oracle.sql.DATE")) {
            String metaDataClassName = rs.getMetaData().getColumnClassName(index);
            if ("java.sql.Timestamp".equals(metaDataClassName) ||
                    "oracle.sql.TIMESTAMP".equals(metaDataClassName)) obj = rs.getTimestamp(index);
            else obj = rs.getDate(index);
        } else if (obj instanceof java.sql.Date) {
            if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) obj = rs.getTimestamp(index);
        } else if (obj instanceof String) obj = ((String) obj).trim();

        return obj;
    }


    public static Object getResultSetValue(ResultSet rs, int index, Class<?> requiredType) throws SQLException {
        if (requiredType == null) return getResultSetValue(rs, index);

        Object value;
        boolean wasNullCheck = false;

        // Explicitly extract typed value, as far as possible.
        if (String.class.equals(requiredType)) {
            switch (rs.getMetaData().getColumnType(index)) {
                case Types.BLOB: // CLOB is treated as String.
                    value = bytesToStr(rs.getBytes(index));
                    break;
                default:
                    value = trim(rs.getString(index));
            }
        } else if (boolean.class.equals(requiredType) || Boolean.class.equals(requiredType)) {
            value = rs.getBoolean(index);
            wasNullCheck = true;
        } else if (byte.class.equals(requiredType) || Byte.class.equals(requiredType)) {
            value = rs.getByte(index);
            wasNullCheck = true;
        } else if (short.class.equals(requiredType) || Short.class.equals(requiredType)) {
            value = rs.getShort(index);
            wasNullCheck = true;
        } else if (int.class.equals(requiredType) || Integer.class.equals(requiredType)) {
            value = rs.getInt(index);
            wasNullCheck = true;
        } else if (long.class.equals(requiredType) || Long.class.equals(requiredType)) {
            value = rs.getLong(index);
            wasNullCheck = true;
        } else if (float.class.equals(requiredType) || Float.class.equals(requiredType)) {
            value = rs.getFloat(index);
            wasNullCheck = true;
        } else if (double.class.equals(requiredType) || Double.class.equals(requiredType) ||
                Number.class.equals(requiredType)) {
            value = rs.getDouble(index);
            wasNullCheck = true;
        } else if (byte[].class.equals(requiredType)) value = rs.getBytes(index);
        else if (java.util.Date.class.equals(requiredType)) {
            value = rs.getTimestamp(index);
            if (value != null && !rs.wasNull()) value = new java.util.Date(((Timestamp) value).getTime());
        } else if (java.sql.Date.class.equals(requiredType)) {
            value = rs.getTimestamp(index);
            if (value != null && !rs.wasNull()) value = new java.sql.Date(((Timestamp) value).getTime());
        } else if (java.sql.Time.class.equals(requiredType)) value = rs.getTime(index);
        else if (java.sql.Timestamp.class.equals(requiredType) || java.util.Date.class.equals(requiredType)) value = rs
                .getTimestamp(index);
        else if (BigDecimal.class.equals(requiredType)) value = rs.getBigDecimal(index);
        else if (Blob.class.equals(requiredType)) value = rs.getBlob(index);
        else if (Clob.class.equals(requiredType)) value = rs.getClob(index);
        else // Some unknown type desired -> rely on getObject.
            value = getResultSetValue(rs, index);

        // Perform was-null check if demanded (for results that the
        // JDBC driver returns as primitives).
        if (wasNullCheck && value != null && rs.wasNull()) value = null;
        return value;
    }

    public static String bytesToStr(byte[] bytes) {
        return new String(bytes, Charsets.UTF_8);
    }


    public static boolean isUpdateStmt(EqlRun sqlSub) {
        switch (sqlSub.getSqlType()) {
            case UPDATE:
            case MERGE:
            case DELETE:
            case INSERT:
                return true;
            default:
                break;
        }
        return false;
    }


    public static boolean isDdl(EqlRun eqlRun) {
        switch (eqlRun.getSqlType()) {
            case CREATE:
            case DROP:
            case TRUNCATE:
            case ALTER:
            case COMMENT:
                return true;
            default:
                break;
        }
        return false;
    }

    public static String autoTrimLastUnusedPart(String sql) {
        String returnSql = trimRight(sql);
        String upper = upperCase(returnSql);
        if (endsWith(upper, "WHERE"))
            return returnSql.substring(0, sql.length() - "WHERE".length());

        if (endsWith(upper, "AND"))
            return returnSql.substring(0, sql.length() - "AND".length());

        if (endsWith(upper, "OR"))
            return returnSql.substring(0, sql.length() - "AND".length());

        return returnSql;
    }

    private static boolean endsWith(String str, String end) {
        return str != null ? str.endsWith(end) : false;
    }

    private static String upperCase(String str) {
        return str != null ? str.toUpperCase() : null;
    }


    public static Object createSingleBean(Object[] params) {
        if (params == null || params.length == 0) return new Object();

        if (params.length > 1) return ImmutableMap.of("_params", params);

        // 只剩下length == 1的情况
        Object param = params[0];
        if (param == null
                || param.getClass().isPrimitive()
                || Primitives.isWrapperType(param.getClass())
                || param instanceof String
                || param.getClass().isArray()
                || param instanceof List) {
            return ImmutableMap.of("_params", params);
        }

        return param;
    }

    public static void closeQuietly(Statement stmt) {
        if (stmt == null) return;

        try {
            stmt.close();
        } catch (SQLException e) {
            // Ignore
        }
    }

    public static void closeQuietly(ResultSet rs, Statement ps) {
        closeQuietly(rs);
        closeQuietly(ps);

    }

    public static void closeQuietly(ResultSet rs) {
        if (rs == null) return;

        try {
            rs.close();
        } catch (SQLException e) {
            // Ignore
        }
    }

    public static void closeQuietly(EqlTran eqlTran) {
        if (eqlTran == null) return;

        try {
            eqlTran.close();
        } catch (IOException e) {
            // Ignore
        }
    }


    /**
     * 缺省的日期时间显示格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static String toDateTimeStr(Timestamp date) {
        return new SimpleDateFormat(DEFAULT_DATETIME_FORMAT).format(date);
    }

    public static byte[] toBytes(String value) {
        return value.getBytes(Charsets.UTF_8);
    }

    public static String convertUnderscoreNameToPropertyName(String name) {
        StringBuilder result = new StringBuilder();
        boolean nextIsUpper = false;
        if (name != null && name.length() > 0) {
            if (name.length() > 1 && name.substring(1, 2).equals("_")) {
                result.append(name.substring(0, 1).toUpperCase());
            } else {
                result.append(name.substring(0, 1).toLowerCase());
            }
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                if (s.equals("_")) {
                    nextIsUpper = true;
                } else {
                    if (nextIsUpper) {
                        result.append(s.toUpperCase());
                        nextIsUpper = false;
                    } else {
                        result.append(s.toLowerCase());
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * Load a class given its name. BL: We wan't to use a known ClassLoader--hopefully the heirarchy is set correctly.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> tryLoadClass(String className) {
        if (Strings.isNullOrEmpty(className)) return null;

        try {
            return (Class<T>) getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return null;
    }


    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    public static boolean isNotEmpty(Object obj) {
        return isNotNull(obj) ? !obj.toString().equals("") : false;
    }


    private static boolean startsWith(String str, String start) {
        return str != null ? str.startsWith(start) : false;
    }

    public static int indexOfBlank(CharSequence cs) {
        int sz = cs.length();
        for (int i = 0; i < sz; i++)
            if (Character.isWhitespace(cs.charAt(i))) return i;

        return -1;
    }

    /**
     * Convert a name in camelCase to an underscored name in lower case.
     * Any upper case letters are converted to lower case with a preceding underscore.
     *
     * @param name the string containing original name
     * @return the converted name
     */
    public static String underscore(String name) {
        StringBuilder result = new StringBuilder();
        if (name != null && name.length() > 0) {
            result.append(name.substring(0, 1).toLowerCase());
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                if (s.equals(s.toUpperCase())) {
                    result.append("_");
                    result.append(s.toLowerCase());
                } else result.append(s);
            }
        }

        return result.toString();
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

    public static void closeQuietly(Closeable writer) {
        if (writer == null) return;

        try {
            writer.close();
        } catch (IOException e) {
            // Ignore
        }

    }

    public static boolean equalsIgnoreCase(String s1, String s2) {
        return s1 != null ? s1.equalsIgnoreCase(s2) : s2 == null;
    }

    public static boolean containsIgnoreCase(String string, String value) {
        return string == null ? false : string.toUpperCase().contains(value.toUpperCase());
    }


    /**
     * Examines a Throwable object and gets it's root cause
     *
     * @param t - the exception to examine
     * @return The root cause
     */
    public static Throwable unwrapThrowable(Throwable t) {
        Throwable t2 = t;
        while (true) {
            if (t2 instanceof InvocationTargetException) {
                t2 = ((InvocationTargetException) t).getTargetException();
            } else if (t instanceof UndeclaredThrowableException) {
                t2 = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
            } else {
                return t2;
            }
        }
    }


    public static String cleanQuote(String option) {
        if (option == null) return "";

        String ret = option;
        if (option.startsWith("\"")) ret = ret.substring(1);
        if (option.endsWith("\"")) ret = ret.substring(0, ret.length() - 1);

        return ret;
    }

    public static void closeQuietly(Statement cs, Connection connection) {
        closeQuietly(cs);
        closeQuietly(connection);
    }

    public static void closeQuietly(Connection connection) {
        if (connection == null) return;

        try {
            connection.close();
        } catch (SQLException e) {
            // Ignore
        }
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

    /**
     * Return the context classloader. BL: if this is command line operation, the classloading issues are more sane.
     * During servlet execution, we explicitly set the ClassLoader.
     *
     * @return The context classloader.
     */
    public static ClassLoader getClassLoader() {
        return Objects.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                EqlUtils.class.getClassLoader());
    }


    public static InputStream classResourceToInputStream(String pathname, boolean silent) {
        InputStream is = classResourceToStream(pathname);
        if (is != null || silent) return is;

        throw new RuntimeException("fail to find " + pathname + " in current dir or classpath");
    }

    public static InputStream classResourceToStream(String resourceName) {
        return getClassLoader().getResourceAsStream(resourceName);
    }

    public static String classResourceToString(String classPath) {
        URL url = getClassLoader().getResource(classPath);
        if (url == null) return null;

        try {
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {

        }

        return null;
    }

    public static String uniqueSqlId(String sqlClassPath, String sqlId) {
        return sqlClassPath + ":" + sqlId;
    }

}
