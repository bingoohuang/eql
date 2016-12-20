package org.n3r.eql.parser;

import com.google.common.collect.Lists;
import org.n3r.eql.map.EqlRun;

import java.util.List;

public class MultiPart implements EqlPart {
    private List<EqlPart> parts = Lists.<EqlPart>newArrayList();

    @Override
    public String evalSql(EqlRun eqlRun) {
        StringBuilder sql = new StringBuilder();
        for (EqlPart eqlPart : parts) {
            appendSpace(sql);
            sql.append(eqlPart.evalSql(eqlRun));
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
