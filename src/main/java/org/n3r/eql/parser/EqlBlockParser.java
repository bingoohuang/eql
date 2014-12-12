package org.n3r.eql.parser;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.util.S;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class EqlBlockParser {
    private final boolean sqlParseDelay;
    private List<Sql> sqls = Lists.newArrayList();
    private DynamicLanguageDriver dynamicLanguageDriver;

    public EqlBlockParser(DynamicLanguageDriver dynamicLanguageDriver, boolean sqlParseDelay) {
        this.dynamicLanguageDriver = dynamicLanguageDriver;
        this.sqlParseDelay = sqlParseDelay;
    }

    public void parse(EqlBlock block, List<String> sqlLines) {
        List<String> onEQLLines = Lists.newArrayList();

        // split to multiple sql
        for (String sqlLine : sqlLines) {
            if (sqlLine.endsWith(block.getSplit())) {
                onEQLLines.add(sqlLine.substring(0, sqlLine.length() - 1));
                addSql(block, onEQLLines);
            } else {
                onEQLLines.add(sqlLine);
            }
        }

        addSql(block, onEQLLines);

        block.setSqls(sqls);
        block.setSqlLines(sqlLines);
    }

    private void addSql(EqlBlock block, List<String> onEQLLines) {
        if (onEQLLines.size() == 0) return;
        if (isAllComments(onEQLLines)) return;

        Sql sql = sqlParseDelay ? new DelaySql(dynamicLanguageDriver, block, new ArrayList<String>(onEQLLines))
                : dynamicLanguageDriver.parse(block, onEQLLines);
        if (sql != null) sqls.add(sql);
        onEQLLines.clear();
    }

    private boolean isAllComments(List<String> onEQLLines) {
        List<String> linesWoLineComments = Lists.newArrayList();

        for (String line : onEQLLines) {
            if (line.startsWith("--")) continue;

            linesWoLineComments.add(line);
        }

        if (linesWoLineComments.size() == 0) return true;

        String join = Joiner.on('\n').join(linesWoLineComments);
        Matcher matcher = ParserUtils.inlineComment.matcher(join);
        String purEQL = matcher.replaceAll("");

        return S.isBlank(purEQL);
    }
}
