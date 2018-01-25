package org.n3r.eql.impl;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class EqlUniqueSqlId {
    private final String sqlClassPath, sqlId;

    public static EqlUniqueSqlId of(String sqlClassPath, String sqlId) {
        return new EqlUniqueSqlId(sqlClassPath, sqlId);
    }

    public EqlUniqueSqlId newTotalRowSqlId() {
        return new EqlUniqueSqlId(sqlClassPath, "__total_rows." + sqlId);
    }
}
