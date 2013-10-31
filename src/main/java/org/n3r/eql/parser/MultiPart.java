package org.n3r.eql.parser;

import com.google.common.collect.Lists;

import java.util.List;

public class MultiPart implements SqlPart {
    private List<SqlPart> parts = Lists.newArrayList();

    @Override
    public String evalSql(Object bean) {
        StringBuilder sql = new StringBuilder();
        for (SqlPart sqlPart : parts) {
            if (sql.length() > 0) sql.append('\n');
            sql.append(sqlPart.evalSql(bean));
        }

        return sql.toString();
    }

    public void addPart(SqlPart part) {
        parts.add(part);
    }

    public int size() {
        return parts.size();
    }

    public SqlPart part(int index) {
        return parts.get(index);
    }
}
