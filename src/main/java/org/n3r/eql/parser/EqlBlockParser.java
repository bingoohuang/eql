package org.n3r.eql.parser;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.util.S;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class EqlBlockParser {
    private DynamicLanguageDriver dynamicLanguageDriver;
    private final boolean sqlParseDelay;
    private List<Sql> sqls = Lists.<Sql>newArrayList();

    public EqlBlockParser(DynamicLanguageDriver dynamicLanguageDriver, boolean sqlParseDelay) {
        this.dynamicLanguageDriver = dynamicLanguageDriver;
        this.sqlParseDelay = sqlParseDelay;
    }

    public void parse(EqlBlock block, List<String> sqlLines) {
        List<String> oneSqlLines = Lists.<String>newArrayList();

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
        if (isAllComments(oneSqlLines)) return;

        Sql sql = sqlParseDelay ? new DelaySql(dynamicLanguageDriver, block, new ArrayList<String>(oneSqlLines))
                : dynamicLanguageDriver.parse(block, oneSqlLines);
        if (sql != null) sqls.add(sql);
        oneSqlLines.clear();
    }

    private boolean isAllComments(List<String> oneSqlLines) {
        List<String> linesWoLineComments = Lists.<String>newArrayList();

        for (String line : oneSqlLines) {
            if (line.startsWith("--")) continue;

            linesWoLineComments.add(line);
        }

        if (linesWoLineComments.size() == 0) return true;

        String join = Joiner.on('\n').join(linesWoLineComments);
        Matcher matcher = ParserUtils.inlineComment.matcher(join);
        String pureSql = matcher.replaceAll("");

        return S.isBlank(pureSql);
    }
}
