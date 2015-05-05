package org.n3r.eql.mtcp.utils;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 处理形如下面的字符串数组组成的表
 * 表的第一行为表字段名称，null表示一行结束。
 * <p/>
 * tenantId, dbtype, host, port, dbname, username, password, null,
 * dba, mysql, localhost, 3306, dba, dba, dba, null,
 * dbb, mysql, localhost, 3306, dbb, dbb, dbb, null,
 * dbc, mysql, localhost, 3306, dbc, dbc, dbc, null,
 * diamond, mysql, localhost, 3306, diamond, diamond, diamond, null
 */

public class StringTable {
    private final String[] table;
    private final int columnCount;

    public StringTable(String tableString) {
        this(convert(tableString));
    }

    public StringTable(String[] table) {
        this.table = convert(table);
        this.columnCount = findColumnCount(table);

        checkTable();
    }

    private static String[] convert(String tableString) {
        String[] parts = tableString.split(",");
        return convert(parts);
    }

    private static String[] convert(String[] table) {
        for (int i = 0, ii = table.length; i < ii; ++i) {
            if ("null".equals(table[i])) table[i] = null;
        }

        return table;
    }

    private static int findColumnCount(String[] table) {
        for (int i = 0, ii = table.length; i < ii; ++i) {
            if (table[i] == null) return i + 1;
        }

        throw new RuntimeException("bad table format: no null to identify rows end");
    }

    private void checkTable() {
        if (table.length % columnCount > 0) {
            throw new RuntimeException("bad table format : columns in rows is not consistent");
        }
    }

    public Map<String, String> findRow(String rowkey) {
        Map<String, String> row = Maps.newHashMap();

        int rowIndex = findRow(table, columnCount, rowkey);

        for (int j = 1; j < columnCount - 1; ++j) {
            String key = table[j];
            String value = table[rowIndex * columnCount + j];
            row.put(key, value);
        }

        return row;
    }

    private int findRow(String[] config, int columnCount, String rowkey) {
        int maxRows = config.length / columnCount;

        for (int row = 1; row < maxRows; ++row) {
            String rowTenantId = config[row * columnCount];
            if (rowkey.equals(rowTenantId)) return row;
        }

        throw new RowNotFoundException("row is not found with rowkey" + rowkey);
    }

    public static class RowNotFoundException extends RuntimeException {
        public RowNotFoundException(String msg) {
            super(msg);
        }
    }
}
