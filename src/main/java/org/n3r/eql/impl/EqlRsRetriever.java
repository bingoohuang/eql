package org.n3r.eql.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.n3r.eql.base.AfterPropertiesSet;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.map.*;
import org.n3r.eql.parser.EqlBlock;
import org.n3r.eql.util.Enums;
import org.n3r.eql.util.O;
import org.n3r.eql.util.Rs;
import org.n3r.eql.util.S;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EqlRsRetriever {
    @Setter private EqlBlock eqlBlock;
    private static int DEFAULT_MAXROWS = 100000;
    @Setter private int maxRows = DEFAULT_MAXROWS;
    @Setter private EqlRowMapper eqlRowMapper;
    @Getter @Setter private String returnTypeName;
    @Getter @Setter private Class<?> returnType;

    public Object convert(ResultSet rs, EqlRun subSql) throws SQLException {
        return maxRows <= 1 || subSql.isWillReturnOnlyOneRow() ? firstRow(rs) : selectList(rs);
    }

    private Object firstRow(ResultSet rs) throws SQLException {
        if (!rs.next()) return null;

        boolean singleColumn = rs.getMetaData().getColumnCount() == 1;
        if (singleColumn) {
            Object resultSetValue = Rs.getResultSetValue(rs, 1);
            Object singleValue = convertSingleValue(resultSetValue, rs);
            return singleValue;
        }

        EqlRowMapper rowMapper = getRowMapper(rs.getMetaData());
        Object o = rowBeanCreate(rowMapper, singleColumn, rs, 1);
        return mapResult(o, rowMapper);
    }

    public Object selectRow(ResultSet rs, int rowIndex) throws SQLException {
        if (rowIndex > maxRows || !rs.next()) return null;

        EqlRowMapper rowMapper = getRowMapper(rs.getMetaData());
        boolean singleColumn = rs.getMetaData().getColumnCount() == 1;

        return rowBeanCreate(rowMapper, singleColumn, rs, rowIndex);
    }

    private Object selectList(ResultSet rs) throws SQLException {
        List<Object> result = new ArrayList<Object>();

        boolean singleColumn = rs.getMetaData().getColumnCount() == 1;
        EqlRowMapper rowMapper = getRowMapper(rs.getMetaData());

        for (int rowIndex = 1; rs.next() && rowIndex <= maxRows; ++rowIndex) {
            Object rowObject = rowBeanCreate(rowMapper, singleColumn, rs, rowIndex);
            if (rowObject != null) result.add(rowObject);
        }

        return mapResult(result, rowMapper);
    }

    private static Object mapResult(Object result, final EqlRowMapper rowMapper) {
        // TODO: to use asm other than reflection
        Method mappingResult = findEqlMappingResultMethod(rowMapper.getClass());
        if (mappingResult != null)
            return O.invokeMethod(rowMapper, mappingResult).orNull();

        return result;
    }

    private static Method findEqlMappingResultMethod(Class<? extends EqlRowMapper> rowMapperClass) {
        for (Method method : rowMapperClass.getMethods()) {
            if (method.isAnnotationPresent(EqlMappingResult.class)
                    && method.getParameterTypes().length == 0
                    && method.getReturnType() != void.class
                    ) {
                return method;
            }
        }

        return null;
    }

    @SneakyThrows
    private Object rowBeanCreate(EqlRowMapper rowMapper, boolean isSingleColumn, ResultSet rs, int rowNum) {
        Object rowBean = rowMapper.mapRow(rs, rowNum, isSingleColumn);
        if (isSingleColumn) rowBean = convertSingleValue(rowBean, rs);

        if (rowBean instanceof AfterPropertiesSet)
            ((AfterPropertiesSet) rowBean).afterPropertiesSet();

        return rowBean;
    }

    private EqlRowMapper getRowMapper(ResultSetMetaData metaData) throws SQLException {
        if (eqlRowMapper != null) return eqlRowMapper;

        if (returnType == null && eqlBlock != null)
            returnType = eqlBlock.getReturnType();

        if (returnType != null && EqlRowMapper.class.isAssignableFrom(returnType))
            return Reflect.on(returnType).create().get();

        if (returnType != null && !Map.class.isAssignableFrom(returnType))
            return new EqlBeanRowMapper(returnType);

        return metaData.getColumnCount() > 1 ? new EqlMapMapper() : new EqlSingleValueMapper();
    }

    public EqlCallableReturnMapper getCallableReturnMapper() {
        if (returnType == null && eqlBlock != null)
            returnType = eqlBlock.getReturnType();

        if (returnType != null && EqlCallableReturnMapper.class.isAssignableFrom(returnType))
            return Reflect.on(returnType).create().get();

        if (returnType != null && !Map.class.isAssignableFrom(returnType))
            return new EqlCallableResultBeanMapper(returnType);

        return new EqlCallableReturnMapMapper();
    }

    private Object convertSingleValue(Object value, ResultSet rs) throws SQLException {
        if (returnType == null && eqlBlock != null)
            returnType = eqlBlock.getReturnType();

        String returnTypeName = this.returnTypeName;
        if (returnTypeName == null)
            returnTypeName = eqlBlock == null ? null : eqlBlock.getReturnTypeName();

        if (returnType == null && returnTypeName == null) return value;

        if ("string".equalsIgnoreCase(returnTypeName) || returnType == String.class) {
            if (value instanceof byte[]) return S.bytesToStr((byte[]) value);
            return value == null ? null : String.valueOf(value);
        }

        if ("int".equalsIgnoreCase(returnTypeName) || returnType == Integer.class || returnType == int.class) {
            if (value instanceof Number) return ((Number) value).intValue();
            return value == null ? null : Integer.parseInt(value.toString());
        }

        if ("long".equalsIgnoreCase(returnTypeName) || returnType == Long.class || returnType == long.class) {
            if (value instanceof Number) return ((Number) value).longValue();
            return value == null ? null : Long.parseLong(value.toString());
        }

        if ("boolean".equalsIgnoreCase(returnTypeName) || returnType == Boolean.class || returnType == boolean.class) {
            if (value instanceof Number)
                return ((Number) value).shortValue() == 1;
            return value == null ? null : Boolean.parseBoolean(value.toString());
        }

        if (returnType == null && returnTypeName != null) {
            returnType = Reflect.on(returnTypeName).type();
        }
        if (returnType != null && !returnType.isPrimitive()) {
            if (returnType.isEnum() && value instanceof String) {
                return Enums.valueOff((Class<Enum>) returnType, (String) value);
            }

            return new EqlBeanRowMapper(returnType).mapRow(rs, 1, false);
        }

        return value;
    }

    public void resetMaxRows() {
        this.maxRows = DEFAULT_MAXROWS;
    }
}
