package org.n3r.eql.matrix.sqlparser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.google.common.collect.Lists;

import java.util.List;

public class MysqlInsertVisitor extends MysqlMatrixVisitor {


    public boolean visit(MySqlInsertStatement x) {
        String tableName = x.getTableName().getSimpleName();

        if (!ruleSet.relativeTo(tableName)) return false;

        List<SqlFieldIndex> sqlFieldIndexes = Lists.newArrayList();
        for (int i = 0; i < x.getColumns().size(); ++i) {
            SQLExpr columnExpr = x.getColumns().get(i);
            if (columnExpr instanceof SQLIdentifierExpr) {
                String columnName = ((SQLIdentifierExpr) columnExpr).getName();
                if (ruleSet.relativeTo(tableName, columnName)) {
                    SQLExpr valueExpr = x.getValues().getValues().get(i);

                    valueExpr.accept(this);

                    if (valueExpr instanceof SQLVariantRefExpr) {
                        sqlFieldIndexes.add(new SqlFieldIndex(tableName, columnName, variantIndex - 1));
                    }
                }
            }
        }

        this.sqlFieldIndexes = sqlFieldIndexes.toArray(new SqlFieldIndex[0]);


        return false;
    }
}
