package org.n3r.eql.parser;

import org.n3r.eql.map.EqlRun;

public class StaticSql implements Sql {
    private String sql;

    public StaticSql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return sql;
    }
}
