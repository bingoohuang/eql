package org.n3r.eql.matrix.sqlparser;

import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import org.n3r.eql.matrix.RulesSet;

public class MysqlMatrixVisitor extends MySqlASTVisitorAdapter {
    public RulesSet ruleSet;
    public SqlFieldIndex[] sqlFieldIndexes;
    public int variantIndex = 0;


    @Override
    public boolean visit(SQLVariantRefExpr x) {
        ++variantIndex;
        return true;
    }
}
