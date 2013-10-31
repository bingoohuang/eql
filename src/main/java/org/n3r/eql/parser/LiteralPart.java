package org.n3r.eql.parser;

public class LiteralPart implements SqlPart {
    private String sql;

    public LiteralPart(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public void appendComment(String comment) {
        if (comment.startsWith("--")) {
            sql += "\n" + comment + "\n";
        } else {
            sql += comment;
        }
    }

    public void appendSql(String line) {
        if (sql.length() > 0)
            sql += " " + line;
        else
            sql = line;
    }

    @Override
    public String evalSql(Object bean) {
        return sql.trim();
    }
}
