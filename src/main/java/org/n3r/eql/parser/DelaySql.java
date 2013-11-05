package org.n3r.eql.parser;

import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.map.EqlRun;

import java.util.List;

public class DelaySql implements Sql {
    private final DynamicLanguageDriver dynamicLanguageDriver;
    private final EqlBlock block;
    private final List<String> oneSqlLines;
    private Sql sql;

    public DelaySql(DynamicLanguageDriver dynamicLanguageDriver,
                    EqlBlock block, List<String> oneSqlLines) {
        this.dynamicLanguageDriver = dynamicLanguageDriver;
        this.block = block;
        this.oneSqlLines = oneSqlLines;
    }

    public void parseSql() {
        sql = dynamicLanguageDriver.parse(block, oneSqlLines);
    }

    @Override
    public String evalSql(EqlRun eqlRun) {
        return sql.evalSql(eqlRun);
    }
}
