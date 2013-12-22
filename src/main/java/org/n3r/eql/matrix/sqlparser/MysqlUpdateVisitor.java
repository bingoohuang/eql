package org.n3r.eql.matrix.sqlparser;

import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;

public class MysqlUpdateVisitor extends MysqlSelectVisitor {
    @Override
    public boolean visit(MySqlUpdateStatement x) {
        x.getTableSource().accept(this);

        for (SQLUpdateSetItem item : x.getItems()) {
            item.accept(this);
        }

        acceptWhere(x.getWhere());

        return false;
    }
}
