package org.n3r.eql.map;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.sql.*;

@AllArgsConstructor
public class ResultSetRs implements RsAware {
    private final ResultSet rs;

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return rs.getMetaData();
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return rs.getByte(columnIndex);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return rs.getBytes(columnIndex);
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return rs.getBoolean(columnIndex);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return rs.getShort(columnIndex);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return rs.getInt(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return rs.getFloat(columnIndex);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return rs.getDouble(columnIndex);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return rs.getTimestamp(columnIndex);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return rs.getTime(columnIndex);
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return rs.getDate(columnIndex);
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return rs.getBlob(columnIndex);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return rs.wasNull();
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return rs.getBigDecimal(columnIndex);
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return rs.getClob(columnIndex);
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return rs.getObject(columnIndex);
    }
}
