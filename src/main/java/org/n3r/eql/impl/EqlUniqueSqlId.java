package org.n3r.eql.impl;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class EqlUniqueSqlId {
    private String sqlClassPath, sqlId;

    public EqlUniqueSqlId newTotalRowSqlId() {
        return new EqlUniqueSqlId(sqlClassPath, "__total_rows." + sqlId);
    }
}
