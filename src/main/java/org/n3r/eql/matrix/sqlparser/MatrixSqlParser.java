package org.n3r.eql.matrix.sqlparser;

import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import lombok.val;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.matrix.RuleParser;
import org.n3r.eql.matrix.RulesSet;
import org.n3r.eql.util.S;

public class MatrixSqlParser {
    public MatrixSqlParseResult parse(EqlConfig eqlConfig, String sql) {
        String rulesContent = readRules(eqlConfig);
        RulesSet ruleSet = new RuleParser().parse(rulesContent);

        val parser = new MySqlStatementParser(sql);
        val stmt = parser.parseStatement();

        MysqlMatrixVisitor visitor = null;
        if (stmt instanceof SQLInsertStatement) {
            visitor = new MysqlInsertVisitor();
        } else if (stmt instanceof SQLSelectStatement) {
            visitor = new MysqlSelectVisitor();
        } else if (stmt instanceof SQLUpdateStatement) {
            visitor = new MysqlUpdateVisitor();
        }

        if (visitor != null) {
            visitor.ruleSet = ruleSet;
            stmt.accept(visitor);
            val sqlFieldIndexes = visitor.sqlFieldIndexes;
            if (sqlFieldIndexes != null && sqlFieldIndexes.length > 0) {
                return new DefaultMatrixSqlParseResult(ruleSet, sqlFieldIndexes);
            }
        }

        return MatrixSqlParseNoResult.instance;
    }

    private String readRules(EqlConfig eqlConfig) {
        String rules = eqlConfig.getStr("rules");
        if (!rules.startsWith("diamond:")) return rules;

        String groupDataId = rules.substring("diamond:".length());
        int commaPos = groupDataId.indexOf(',');
        String group = S.trimToEmpty(groupDataId.substring(0, commaPos));
        String dataId = S.trimToEmpty(groupDataId.substring(commaPos + 1));

        return org.n3r.diamond.client.DiamondMiner.getStone(group, dataId);
    }
}
