package org.n3r.eql.pojo.impl;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.n3r.eql.pojo.annotations.EqlColumn;
import org.n3r.eql.pojo.annotations.EqlId;
import org.n3r.eql.pojo.annotations.EqlSkip;
import org.n3r.eql.pojo.annotations.EqlTable;
import org.n3r.eql.util.Names;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class PojoParser {
    static LoadingCache<Class<?>, String> creatEQLCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) {
            return parseCreatEQLWoCache(pojoClass);
        }
    });

    public static String parseCreatEQL(Class<?> pojoClass) {
        return creatEQLCache.getUnchecked(pojoClass);
    }

    private static Field[] parsePropertiesName(Class<?> pojoClass) {
        Field[] declaredFields = pojoClass.getDeclaredFields();
        List<Field> properties = Lists.newArrayList();
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (field.getAnnotation(EqlSkip.class) != null) continue;

            properties.add(field);
        }

        return properties.toArray(new Field[properties.size()]);
    }

    static String parseCreatEQLWoCache(Class<?> pojoClass) {
        String tableName = parseTableName(pojoClass);
        StringBuilder creatEQL = new StringBuilder("insert into ").append(tableName).append("(");
        StringBuilder valuEQL = new StringBuilder(") values(");
        for (Field field : parsePropertiesName(pojoClass)) {
            String columnName = parseColumnName(field);
            creatEQL.append(columnName).append(',');
            valuEQL.append('#').append(field.getName()).append("#,");
        }

        char c = creatEQL.charAt(creatEQL.length() - 1);
        if (c != ',') {
            throw new RuntimeException("there is no property to save for class " + pojoClass);
        }

        creatEQL.delete(creatEQL.length() - 1, creatEQL.length());
        valuEQL.delete(valuEQL.length() - 1, valuEQL.length());
        creatEQL.append(valuEQL).append(')');

        return creatEQL.toString();
    }


    static LoadingCache<Class<?>, String> readSqlCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) {
            return parseReadSqlWoCache(pojoClass);
        }
    });

    public static String parseReadSql(Class<?> pojoClass) {
        return readSqlCache.getUnchecked(pojoClass);
    }

    static String parseReadSqlWoCache(Class<?> pojoClass) {
        StringBuilder selectSql = new StringBuilder("select ");
        StringBuilder wherEQL = new StringBuilder();
        int initialLen = selectSql.length();

        for (Field field : parsePropertiesName(pojoClass)) {
            String columnName = parseColumnName(field);
            if (selectSql.length() > initialLen) selectSql.append(',');
            selectSql.append(columnName).append(" as ").append(field.getName());

            wherEQL.append("-- isNotEmpty ").append(field.getName()).append("\r\n");
            wherEQL.append(" and ").append(columnName).append("=#").append(field.getName()).append("#\r\n");
            wherEQL.append("-- end\r\n");
        }

        String tableName = parseTableName(pojoClass);

        return selectSql.append(" from ").append(tableName)
                .append("\r\n-- trim prefix=where prefixOverrides=and\r\n")
                .append(wherEQL)
                .append("-- end\r\n")
                .toString();
    }


    static LoadingCache<Class<?>, String> updatEQLCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) {
            return parseUpdatEQLWoCache(pojoClass, "");
        }
    });

    public static String parseUpdatEQL(Class<?> pojoClass) {
        return updatEQLCache.getUnchecked(pojoClass);
    }

    static public String PREFIX_FLAG = "_flag_";
    static LoadingCache<Class<?>, String> updatEQLCache2 = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) {
            return parseUpdatEQLWoCache(pojoClass, PREFIX_FLAG);
        }
    });

    public static String parseUpdatEQL2(Class<?> pojoClass) {
        return updatEQLCache2.getUnchecked(pojoClass);
    }

    public static String parseUpdatEQLWoCache(Class<?> pojoClass, String fieldFlagPrefix) {
        StringBuilder setSql = new StringBuilder();
        StringBuilder wherEQL = new StringBuilder();

        for (Field field : parsePropertiesName(pojoClass)) {
            String columnName = parseColumnName(field);
            boolean isIdColumn = isIdColumn(field, columnName);
            if (isIdColumn) {
                if (wherEQL.length() > 0) wherEQL.append(" and ");
                wherEQL.append(columnName).append("=#").append(field.getName()).append("#");
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
                .append("where ").append(wherEQL);

        return sql.toString();
    }

    private static boolean isIdColumn(Field field, String columnName) {
        EqlId idAnnotation = field.getAnnotation(EqlId.class);
        return idAnnotation != null || "id".equals(columnName);
    }

    static LoadingCache<Class<?>, String> deletEQLCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, String>() {
        @Override
        public String load(Class<?> pojoClass) {
            return parseDeletEQLWoCache(pojoClass);
        }
    });

    public static String parseDeletEQL(Class<?> pojoClass) {
        return deletEQLCache.getUnchecked(pojoClass);
    }

    static String parseDeletEQLWoCache(Class<?> pojoClass) {
        String tableName = parseTableName(pojoClass);
        StringBuilder sql = new StringBuilder("delete from ").append(tableName).append(" where ");
        int initialLen = sql.length();

        for (Field field : parsePropertiesName(pojoClass)) {
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
                ? Names.convertCamelToUnderscore(pojoClass.getSimpleName())
                : pojoClassAnnotation.name();
    }


    private static String parseColumnName(Field field) {
        EqlColumn column = field.getAnnotation(EqlColumn.class);
        return column != null ? column.name() : Names.convertCamelToUnderscore(field.getName());
    }
}
