package org.n3r.eql.util;

import org.n3r.eql.map.ResultSetRs;
import org.n3r.eql.map.RsAware;

import java.math.BigDecimal;
import java.sql.*;

public class Rs {

    public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) name = resultSetMetaData.getColumnName(columnIndex);
        return name;
    }

    public static Object getResultSetValue(RsAware rs, int index) throws SQLException {
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

    public static Object getResultSetValue(RsAware rs, int index, Class<?> requiredType) throws SQLException {
        if (requiredType == null) return getResultSetValue(rs, index);

        Object value;
        boolean wasNullCheck = false;

        // Explicitly extract typed value, as far as possible.
        if (String.class.equals(requiredType)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnType = metaData != null ? metaData.getColumnType(index) : Types.VARCHAR;
            switch (columnType) {
                case Types.BLOB: // CLOB is treated as String.
                    value = S.bytesToStr(rs.getBytes(index));
                    break;
                default:
                    value = S.trim(rs.getString(index));
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

    public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
        return getResultSetValue(new ResultSetRs(rs), index);
    }
}
