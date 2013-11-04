package org.n3r.eql.parser;

import com.google.common.collect.Lists;
import org.n3r.eql.base.DynamicLanguageDriver;

import java.util.List;

public class EqlBlockParser {
    private List<Sql> sqls = Lists.newArrayList();
    private DynamicLanguageDriver dynamicLanguageDriver;

    public EqlBlockParser(DynamicLanguageDriver dynamicLanguageDriver) {
        this.dynamicLanguageDriver = dynamicLanguageDriver;
    }

    public void parse(EqlBlock block, List<String> sqlLines) {
        List<String> oneSqlLines = Lists.newArrayList();

        // split to multiple sql
        for (String sqlLine : sqlLines) {
            if (sqlLine.endsWith(block.getSplit())) {
                oneSqlLines.add(sqlLine.substring(0, sqlLine.length() - 1));
                addSql(block, oneSqlLines);
            } else {
                oneSqlLines.add(sqlLine);
            }
        }

        addSql(block, oneSqlLines);

        block.setSqls(sqls);
        block.setSqlLines(sqlLines);
    }

    private void addSql(EqlBlock block, List<String> oneSqlLines) {
        if (oneSqlLines.size() == 0) return;

        Sql sql = dynamicLanguageDriver.parse(block, oneSqlLines);
        if (sql != null) sqls.add(sql);
        oneSqlLines.clear();
    }
}
