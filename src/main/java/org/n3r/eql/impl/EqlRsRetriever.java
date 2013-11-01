package org.n3r.eql.impl;

import org.n3r.eql.base.AfterPropertiesSet;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.map.*;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EqlRsRetriever {
    private EqlBlock eqlBlock;
    private static int DEFAULT_MAXROWS = 100000;
    private int maxRows = DEFAULT_MAXROWS;
    private EqlRowMapper eqlRowMapper;
    private String returnTypeName;
    private Class<?> returnType;

    public Object convert(ResultSet rs, EqlRun subSql) throws SQLException {
        return maxRows <= 1 || subSql.isWillReturnOnlyOneRow() ? firstRow(rs) : selectList(rs);
    }

    private Object firstRow(ResultSet rs) throws SQLException {
        if (!rs.next()) return null;

        if (rs.getMetaData().getColumnCount() == 1)
            return convertSingleValue(EqlUtils.getResultSetValue(rs, 1));

        return rowBeanCreate(rs, 1);
    }

    public Object selectRow(ResultSet rs, int rownum) throws SQLException {
        return rownum <= maxRows && rs.next() ? rowBeanCreate(rs, rownum) : null;
    }

    private Object selectList(ResultSet rs) throws SQLException {
        List<Object> result = new ArrayList<Object>();

        for (int rownum = 1; rs.next() && rownum <= maxRows; ++rownum) {
            Object rowObject = rowBeanCreate(rs, rownum);
            if (rowObject != null) result.add(rowObject);
        }

        return result;
    }

    private Object rowBeanCreate(ResultSet rs, int rowNum) throws SQLException {
        Object rowBean = getRowMapper(rs.getMetaData()).mapRow(rs, rowNum);
        if (rowBean instanceof AfterPropertiesSet)
            ((AfterPropertiesSet) rowBean).afterPropertiesSet();

        return rowBean;
    }

    private EqlRowMapper getRowMapper(ResultSetMetaData metaData) throws SQLException {
        if (eqlRowMapper != null) return eqlRowMapper;

        if (returnType == null && eqlBlock != null) returnType = eqlBlock.getReturnType();

        if (returnType != null && EqlRowMapper.class.isAssignableFrom(returnType))
            return Reflect.on(returnType).create().get();

        if (returnType != null) return new EqlBeanRowMapper(returnType);

        return metaData.getColumnCount() > 1 ? new EqlMapMapper() : new EqlSingleValueMapper();
    }

    public EqlCallableReturnMapper getCallableReturnMapper() {
        if (returnType == null && eqlBlock != null) returnType = eqlBlock.getReturnType();

        if (returnType != null && EqlCallableReturnMapper.class.isAssignableFrom(returnType))
            return Reflect.on(returnType).create().get();

        if (returnType != null) return new EqlCallableResultBeanMapper(returnType);

        return new EqlCallableReturnMapMapper();
    }

    private Object convertSingleValue(Object value) {
        if (value == null) return null;

        if (returnType == null && eqlBlock != null) returnType = eqlBlock.getReturnType();

        String returnTypeName = this.returnTypeName;
        if (returnTypeName == null)
            returnTypeName = eqlBlock == null ? null : eqlBlock.getOptions().get("returnType");

        if (returnType == null && returnTypeName == null) return value;

        if ("string".equalsIgnoreCase(returnTypeName)) {
            if (value instanceof byte[]) return EqlUtils.bytesToStr((byte[]) value);

            return String.valueOf(value);
        }

        if ("int".equalsIgnoreCase(returnTypeName)) {
            if (value instanceof Number) return ((Number) value).intValue();
            return Integer.parseInt(value.toString());
        }

        if ("long".equalsIgnoreCase(returnTypeName)) {
            if (value instanceof Number) return ((Number) value).longValue();
            return Long.parseLong(value.toString());
        }

        return value;
    }

    public void setEqlBlock(EqlBlock eqlBlock) {
        this.eqlBlock = eqlBlock;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }


    public void setEqlRowMapper(EqlRowMapper eqlRowMapper) {
        this.eqlRowMapper = eqlRowMapper;
    }

    public void setReturnTypeName(String returnTypeName) {
        this.returnTypeName = returnTypeName;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public void resetMaxRows() {
        this.maxRows = DEFAULT_MAXROWS;
    }
}
