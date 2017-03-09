package org.n3r.eql.matrix.sqlparser;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.google.common.collect.Lists;
import lombok.val;

public class MysqlInsertVisitor extends MysqlMatrixVisitor {

    public boolean visit(MySqlInsertStatement x) {
        String tableName = x.getTableName().getSimpleName();

        if (!ruleSet.relativeTo(tableName)) return false;

        val sqlFieldIndexes = Lists.<SqlFieldIndex>newArrayList();
        for (int i = 0, ii = x.getColumns().size(); i < ii; ++i) {
            val columnExpr = x.getColumns().get(i);
            if (!(columnExpr instanceof SQLIdentifierExpr)) continue;

            String columnName = ((SQLIdentifierExpr) columnExpr).getName();
            val valueExpr = x.getValues().getValues().get(i);
            valueExpr.accept(this);

            if (!ruleSet.relativeTo(tableName, columnName)) continue;
            if (!(valueExpr instanceof SQLVariantRefExpr)) continue;

            val index = new SqlFieldIndex(tableName, columnName, variantIndex - 1);
            sqlFieldIndexes.add(index);
        }

        this.sqlFieldIndexes = sqlFieldIndexes.toArray(new SqlFieldIndex[0]);


        return false;
    }
}
