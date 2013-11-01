package org.n3r.eql.parser;

import java.util.Map;

public interface Sql {
    String evalSql(Object bean, Map<String, Object> executionContext);
}
