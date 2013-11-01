package org.n3r.eql.parser;

public class StaticEql implements Eql {
    private String sql;

    public StaticEql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String evalSql(Object bean) {
        return sql;
    }
}
