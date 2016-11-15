package org.n3r.eql.matrix.sqlparser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;

public class MysqlSelectVisitor extends MysqlMatrixVisitor {
    protected final Map<String, String> aliasTablesMap = Maps.newHashMap();

    protected SqlFieldIndex sqlFieldIndex;

    protected List<SqlFieldIndex> sqlFieldIndexeList;

    @Override
    public boolean visit(MySqlSelectQueryBlock x) {
        for (SQLSelectItem item : x.getSelectList()) {
            item.accept(this);
        }

        if (x.getFrom() != null) {
            x.getFrom().accept(this);
        }

        acceptWhere(x.getWhere());

        return false;
    }

    protected void acceptWhere(SQLExpr where) {
        if (where != null) {
            sqlFieldIndexeList = Lists.newArrayList();
            where.accept(this);
            sqlFieldIndexes = sqlFieldIndexeList.toArray(new SqlFieldIndex[0]);
        }
    }

    @Override
    public boolean visit(SQLBinaryOpExpr x) {
        if (x.getOperator() != SQLBinaryOperator.Equality) return true;

        if (hasSecuretField(x.getLeft()) && x.getRight() instanceof SQLVariantRefExpr
                || hasSecuretField(x.getRight()) && x.getLeft() instanceof SQLVariantRefExpr) {
            ++variantIndex;
            sqlFieldIndexeList.add(sqlFieldIndex);
        }

        return false;
    }

    private boolean hasSecuretField(SQLExpr field) {
        return field instanceof SQLIdentifierExpr && isSecuretField((SQLIdentifierExpr) field)
                || field instanceof SQLPropertyExpr && isSecuretField((SQLPropertyExpr) field);
    }

    private boolean isSecuretField(SQLIdentifierExpr field) {
        String oneTableName = getOneTableName();
        return oneTableName != null && containsInSecuretFields(oneTableName, field.getName());
    }

    private boolean isSecuretField(SQLPropertyExpr expr) {
        String tableName = aliasTablesMap.get(expr.getOwner().toString());
        String fieldName = expr.getName();

        return containsInSecuretFields(tableName, fieldName);
    }

    private boolean containsInSecuretFields(String tableName, String fieldName) {
        this.sqlFieldIndex = new SqlFieldIndex(tableName, fieldName, variantIndex);
        return ruleSet.relativeTo(tableName, fieldName);
    }


    private String getOneTableName() {
        if (aliasTablesMap.size() == 1)
            for (Map.Entry<String, String> entry : aliasTablesMap.entrySet())
                return entry.getValue();

        return null;
    }

    @Override
    public boolean visit(SQLExprTableSource source) {
        if (source.getExpr() instanceof SQLIdentifierExpr)
            addTableAlias(source, (SQLIdentifierExpr) source.getExpr());

        return true;
    }

    private void addTableAlias(SQLTableSource from, SQLIdentifierExpr expr) {
        addTableAlias(from.getAlias(), expr);
    }

    private void addTableAlias(String alias, SQLIdentifierExpr expr) {
        addTableAlias(alias, expr.getName());
    }

    private void addTableAlias(String alias, String tableName) {
        aliasTablesMap.put(firstNonNull(alias, tableName), tableName);
    }

}
