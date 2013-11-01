package org.n3r.eql.parser;

import java.util.Map;

public class StaticSql implements Sql {
    private String sql;

    public StaticSql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        return sql;
    }
}
