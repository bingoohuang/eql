package org.n3r.eql.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EqlRsRetriever {
    @Setter
    private EqlBlock eqlBlock;
    private static int DEFAULT_MAX_ROWS = 100000;
    @Setter
    private int maxRows = DEFAULT_MAX_ROWS;
    @Setter
    private EqlRowMapper eqlRowMapper;
    @Getter
    @Setter
    private String returnTypeName;
    @Getter
    @Setter
    private Class<?> returnType;

    public Object convert(ResultSet rs, EqlRun subSql) {
        return maxRows <= 1 || subSql.isWillReturnOnlyOneRow()
                ? firstRow(rs) : selectList(rs);
    }

    @SneakyThrows
    private Object firstRow(ResultSet rs) {
        if (!rs.next()) return null;

        val singleColumn = rs.getMetaData().getColumnCount() == 1;
        if (singleColumn) {
            val resultSetValue = Rs.getResultSetValue(rs, 1);
            val singleValue = convertSingleValue(resultSetValue, rs);
            return singleValue;
        }

        val rowMapper = getRowMapper(rs.getMetaData());
        val o = rowBeanCreate(rowMapper, false, rs, 1);
        return mapResult(o, rowMapper);
    }

    @SneakyThrows
    public Object selectRow(ResultSet rs, int rowIndex) {
        if (rowIndex > maxRows || !rs.next()) return null;

        val rowMapper = getRowMapper(rs.getMetaData());
        val singleColumn = rs.getMetaData().getColumnCount() == 1;

        return rowBeanCreate(rowMapper, singleColumn, rs, rowIndex);
    }

    @SneakyThrows
    private Object selectList(ResultSet rs) {
        List<Object> result = new ArrayList<>();

        val singleColumn = rs.getMetaData().getColumnCount() == 1;
        val rowMapper = getRowMapper(rs.getMetaData());

        for (int rowIndex = 1; rs.next() && rowIndex <= maxRows; ++rowIndex) {
            val rowObject = rowBeanCreate(rowMapper, singleColumn, rs, rowIndex);
            if (rowObject != null) result.add(rowObject);
        }

        return mapResult(result, rowMapper);
    }

    private static Object mapResult(Object result, final EqlRowMapper rowMapper) {
        // TODO: to use asm other than reflection
        val mappingResult = findEqlMappingResultMethod(rowMapper.getClass());
        return mappingResult == null ? result
                : O.invokeMethod(rowMapper, mappingResult).orElse(null);
    }

    private static Method findEqlMappingResultMethod(
            Class<? extends EqlRowMapper> rowMapperClass) {
        for (val method : rowMapperClass.getMethods()) {
            if (method.isAnnotationPresent(EqlMappingResult.class)
                    && method.getParameterTypes().length == 0
                    && method.getReturnType() != void.class) {
                return method;
            }
        }

        return null;
    }

    @SneakyThrows
    private Object rowBeanCreate(
            EqlRowMapper rowMapper,
            boolean isSingleColumn, ResultSet rs, int rowNum) {
        Object rowBean = rowMapper.mapRow(rs, rowNum, isSingleColumn);
        if (isSingleColumn) rowBean = convertSingleValue(rowBean, rs);

        if (rowBean instanceof AfterPropertiesSet)
            ((AfterPropertiesSet) rowBean).afterPropertiesSet();

        return rowBean;
    }

    @SneakyThrows
    private EqlRowMapper getRowMapper(ResultSetMetaData metaData) {
        if (eqlRowMapper != null) return eqlRowMapper;

        if (returnType == null && eqlBlock != null)
            returnType = eqlBlock.getReturnType();

        if (returnType != null && EqlRowMapper.class.isAssignableFrom(returnType))
            return Reflect.on(returnType).create().get();

        if (returnType != null && !Map.class.isAssignableFrom(returnType))
            return new EqlBeanRowMapper(returnType);

        return metaData.getColumnCount() > 1
                ? new EqlMapMapper() : new EqlSingleValueMapper();
    }

    public EqlCallableReturnMapper getCallableReturnMapper() {
        if (returnType == null && eqlBlock != null)
            returnType = eqlBlock.getReturnType();

        if (returnType != null
                && EqlCallableReturnMapper.class.isAssignableFrom(returnType))
            return Reflect.on(returnType).create().get();

        if (returnType != null && !Map.class.isAssignableFrom(returnType))
            return new EqlCallableResultBeanMapper(returnType);

        return new EqlCallableReturnMapMapper();
    }

    @SneakyThrows @SuppressWarnings("unchecked")
    private Object convertSingleValue(Object value, ResultSet rs) {
        if (value == null) return null;

        if (returnType == null && eqlBlock != null)
            returnType = eqlBlock.getReturnType();

        String returnTypeName = this.returnTypeName;
        if (returnTypeName == null)
            returnTypeName = eqlBlock == null ? null : eqlBlock.getReturnTypeName();

        if (returnType == null && returnTypeName == null) return value;

        Object x = processString(value, returnTypeName);
        if (x != null) return x;
        x = processInt(value, returnTypeName);
        if (x != null) return x;
        x = processLong(value, returnTypeName);
        if (x != null) return x;


        x = processBoolean(value, returnTypeName);
        if (x != null) return x;
        x = processDouble(value, returnTypeName);
        if (x != null) return x;
        x = processFloat(value, returnTypeName);
        if (x != null) return x;
        x = processShort(value, returnTypeName);
        if (x != null) return x;

        if (returnType == null && returnTypeName != null) {
            returnType = Reflect.on(returnTypeName).type();
        }

        if (returnType != null && !returnType.isPrimitive()) {
            if (returnType.isEnum() && value instanceof String) {
                return Enums.valueOff((Class<Enum>) returnType, (String) value);
            }

            if (returnType == Timestamp.class) {
                return rs.getTimestamp(1);
            }

            val mapper = MapperFactoryCache.getFromDbMapper(returnType);
            if (mapper.isPresent()) {
                return mapper.get().map(new ResultSetRs(rs), 1);
            }

            return new EqlBeanRowMapper(returnType)
                    .mapRow(rs, 1, false);
        }

        return value;
    }

    private Object processString(Object value, String returnTypeName) {
        if ("string".equalsIgnoreCase(returnTypeName) || returnType == String.class) {
            if (value instanceof byte[]) return S.bytesToStr((byte[]) value);
            return String.valueOf(value);
        }
        return null;
    }

    private Object processInt(Object value, String returnTypeName) {
        if ("int".equalsIgnoreCase(returnTypeName)
                || returnType == Integer.class || returnType == int.class) {
            if (value instanceof Number) return ((Number) value).intValue();
            return Integer.parseInt(value.toString());
        }
        return null;
    }

    private Object processLong(Object value, String returnTypeName) {
        if ("long".equalsIgnoreCase(returnTypeName)
                || returnType == Long.class || returnType == long.class) {
            if (value instanceof Number) return ((Number) value).longValue();
            return Long.parseLong(value.toString());
        }
        return null;
    }

    private Object processBoolean(Object value, String returnTypeName) {
        if ("boolean".equalsIgnoreCase(returnTypeName)
                || returnType == Boolean.class || returnType == boolean.class) {
            if (value instanceof Number)
                return ((Number) value).shortValue() == 1;
            return Boolean.parseBoolean(value.toString());
        }
        return null;
    }

    private Object processDouble(Object value, String returnTypeName) {
        if ("double".equalsIgnoreCase(returnTypeName)
                || returnType == Double.class || returnType == double.class) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            return Double.parseDouble(value.toString());
        }
        return null;
    }

    private Object processFloat(Object value, String returnTypeName) {
        if ("float".equalsIgnoreCase(returnTypeName)
                || returnType == Float.class || returnType == float.class) {
            if (value instanceof Number) return ((Number) value).floatValue();
            return Float.parseFloat(value.toString());
        }
        return null;
    }

    private Object processShort(Object value, String returnTypeName) {
        if ("short".equalsIgnoreCase(returnTypeName)
                || returnType == Short.class || returnType == short.class) {
            if (value instanceof Number) return ((Number) value).shortValue();
            return Short.parseShort(value.toString());
        }
        return null;
    }


    public void resetMaxRows() {
        this.maxRows = DEFAULT_MAX_ROWS;
    }
}
