package org.n3r.eql.map;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public interface RsAware {
    ResultSetMetaData getMetaData() throws SQLException;

    byte getByte(int columnIndex) throws SQLException;

    byte[] getBytes(int columnIndex) throws SQLException;

    String getString(int columnIndex) throws SQLException;

    boolean getBoolean(int columnIndex) throws SQLException;

    short getShort(int columnIndex) throws SQLException;

    int getInt(int columnIndex) throws SQLException;

    long getLong(int columnIndex) throws SQLException;

    float getFloat(int columnIndex) throws SQLException;

    double getDouble(int columnIndex) throws SQLException;

    java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException;

    java.sql.Time getTime(int columnIndex) throws SQLException;

    java.sql.Date getDate(int columnIndex) throws SQLException;

    Blob getBlob(int columnIndex) throws SQLException;

    boolean wasNull() throws SQLException;

    BigDecimal getBigDecimal(int columnIndex) throws SQLException;

    Clob getClob(int columnIndex) throws SQLException;

    Object getObject(int columnIndex) throws SQLException;
}
