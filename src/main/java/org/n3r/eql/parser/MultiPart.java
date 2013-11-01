package org.n3r.eql.parser;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class MultiPart implements EqlPart {
    private List<EqlPart> parts = Lists.newArrayList();

    @Override
    public String evalSql(Object bean, Map<String, Object> executionContext) {
        StringBuilder sql = new StringBuilder();
        for (EqlPart eqlPart : parts) {
            appendSpace(sql);
            sql.append(eqlPart.evalSql(bean, executionContext));
        }

        return sql.toString();
    }

    private void appendSpace(StringBuilder sql) {
        if (sql.length() == 0) return;
        char c = sql.charAt(sql.length() - 1);
        if (!Character.isWhitespace(c)) sql.append(' ');
    }

    public void addPart(EqlPart part) {
        parts.add(part);
    }

    public int size() {
        return parts.size();
    }

    public EqlPart part(int index) {
        return parts.get(index);
    }
}
