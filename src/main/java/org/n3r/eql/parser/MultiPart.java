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
            sql.append(eqlPart.evalSql(bean, executionContext));
        }

        return sql.toString();
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
