package org.n3r.eql.parser;

public class StaticSql implements Sql {
    private String sql;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String evalSql(Object bean) {
        return sql;
    }
}
