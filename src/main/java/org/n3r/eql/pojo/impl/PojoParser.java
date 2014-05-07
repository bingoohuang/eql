package org.n3r.eql.pojo.impl;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.pojo.annotations.EqlColumn;
import org.n3r.eql.pojo.annotations.EqlId;
import org.n3r.eql.pojo.annotations.EqlSkip;
import org.n3r.eql.pojo.annotations.EqlTable;
import org.n3r.eql.util.EqlUtils;

import java.lang.reflect.Field;

public class PojoParser {
    static LoadingCache<Class<?>, String> createSqlCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) throws Exception {
            return parseCreateSqlWoCache(pojoClass);
        }
    });

    public static String parseCreateSql(Class<?> pojoClass) {
        return createSqlCache.getUnchecked(pojoClass);
    }

    static String parseCreateSqlWoCache(Class<?> pojoClass) {
        String tableName = parseTableName(pojoClass);
        StringBuilder createSql = new StringBuilder("insert into ").append(tableName).append("(");
        StringBuilder valueSql = new StringBuilder(") values(");
        Field[] declaredFields = pojoClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getAnnotation(EqlSkip.class) != null) continue;

            String columnName = parseColumnName(field);
            createSql.append(columnName).append(',');
            valueSql.append('#').append(field.getName()).append("#,");
        }

        char c = createSql.charAt(createSql.length() - 1);
        if (c != ',') {
            throw new RuntimeException("there is no property to save for class " + pojoClass);
        }

        createSql.delete(createSql.length() - 1, createSql.length());
        valueSql.delete(valueSql.length() - 1, valueSql.length());
        createSql.append(valueSql).append(')');

        return createSql.toString();
    }


    static LoadingCache<Class<?>, String> readSqlCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) throws Exception {
            return parseReadSqlWoCache(pojoClass);
        }
    });

    public static String parseReadSql(Class<?> pojoClass) {
        return readSqlCache.getUnchecked(pojoClass);
    }

    static String parseReadSqlWoCache(Class<?> pojoClass) {
        StringBuilder selectSql = new StringBuilder("select ");
        StringBuilder whereSql = new StringBuilder();
        int initialLen = selectSql.length();

        Field[] declaredFields = pojoClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getAnnotation(EqlSkip.class) != null) continue;

            String columnName = parseColumnName(field);
            if (selectSql.length() > initialLen) selectSql.append(',');
            selectSql.append(columnName).append(" as ").append(field.getName());

            whereSql.append("-- isNotEmpty ").append(field.getName()).append("\r\n");
            whereSql.append(" and ").append(columnName).append("=#").append(field.getName()).append("#\r\n");
            whereSql.append("-- end\r\n");
        }

        String tableName = parseTableName(pojoClass);

        return selectSql.append(" from ").append(tableName)
                .append("\r\n-- trim prefix=where prefixOverrides=and\r\n")
                .append(whereSql)
                .append("-- end\r\n")
                .toString();
    }


    static LoadingCache<Class<?>, String> updateSqlCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) throws Exception {
            return parseUpdateSqlWoCache(pojoClass, "");
        }
    });

    public static String parseUpdateSql(Class<?> pojoClass) {
        return updateSqlCache.getUnchecked(pojoClass);
    }

    static public String PREFIX_FLAG = "_flag_";
    static LoadingCache<Class<?>, String> updateSqlCache2 = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) throws Exception {
            return parseUpdateSqlWoCache(pojoClass, PREFIX_FLAG);
        }
    });

    public static String parseUpdateSql2(Class<?> pojoClass) {
        return updateSqlCache2.getUnchecked(pojoClass);
    }

    public static String parseUpdateSqlWoCache(Class<?> pojoClass, String fieldFlagPrefix) {
        StringBuilder setSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder();

        Field[] declaredFields = pojoClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getAnnotation(EqlSkip.class) != null) continue;

            String columnName = parseColumnName(field);
            boolean isIdColumn = isIdColumn(field, columnName);
            if (isIdColumn) {
                if (whereSql.length() > 0) whereSql.append(" and ");
                whereSql.append(columnName).append("=#").append(field.getName()).append("#");
            } else {
                setSql.append("-- isNotEmpty ").append(fieldFlagPrefix + field.getName()).append("\r\n");
                setSql.append(columnName).append("=#").append(field.getName()).append("#,\r\n");
                setSql.append("-- end\r\n");
            }
        }

        String tableName = parseTableName(pojoClass);
        StringBuilder sql = new StringBuilder("update ").append(tableName).append("\r\n")
                .append("-- trim prefix=set suffixOverrides=,   \r\n")
                .append(setSql)
                .append("-- end \r\n")
                .append("where ").append(whereSql);

        return sql.toString();
    }

    private static boolean isIdColumn(Field field, String columnName) {
        EqlId idAnnotation = field.getAnnotation(EqlId.class);
        return idAnnotation != null || "id".equals(columnName);
    }

    static LoadingCache<Class<?>, String> deleteSqlCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) throws Exception {
            return parseDeleteSqlWoCache(pojoClass);
        }
    });

    public static String parseDeleteSql(Class<?> pojoClass) {
        return deleteSqlCache.getUnchecked(pojoClass);
    }

    static String parseDeleteSqlWoCache(Class<?> pojoClass) {
        String tableName = parseTableName(pojoClass);
        StringBuilder sql = new StringBuilder("delete from ").append(tableName).append(" where ");
        int initialLen = sql.length();

        Field[] declaredFields = pojoClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getAnnotation(EqlSkip.class) != null) continue;

            String columnName = parseColumnName(field);
            boolean isIdColumn = isIdColumn(field, columnName);

            if (!isIdColumn) continue;

            if (sql.length() > initialLen) sql.append(" and ");
            sql.append(columnName).append("=#").append(field.getName()).append("#");
        }

        return sql.toString();
    }


    private static String parseTableName(Class<?> pojoClass) {
        EqlTable pojoClassAnnotation = pojoClass.getAnnotation(EqlTable.class);
        return (pojoClassAnnotation == null)
                ? EqlUtils.convertCamelToUnderscore(pojoClass.getSimpleName())
                : pojoClassAnnotation.name();
    }


    private static String parseColumnName(Field field) {
        EqlColumn column = field.getAnnotation(EqlColumn.class);
        return column != null ? column.name() : EqlUtils.convertCamelToUnderscore(field.getName());
    }
}
