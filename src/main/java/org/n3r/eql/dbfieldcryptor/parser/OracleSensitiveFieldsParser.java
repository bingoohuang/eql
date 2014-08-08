package org.n3r.eql.dbfieldcryptor.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.*;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleASTVisitorAdapter;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleSensitiveFieldsParser implements SensitiveFieldsParser {
    private final Logger log = LoggerFactory.getLogger(OracleSensitiveFieldsParser.class);

    private final Map<String, Object> aliasTablesMap = Maps.newHashMap();
    private final Set<Integer> securetBindIndice = Sets.newHashSet();
    private final Set<Integer> securetResultIndice = Sets.newHashSet();
    private final Set<String> securetResultLabels = Sets.newHashSet();

    private final List<BindVariant> subQueryBindAndVariantOfFrom = Lists.newArrayList();

    private final Set<String> securetFields;

    private int variantIndex = 0;
    private final String sql;

    private OracleASTVisitorAdapter adapter = new OracleASTVisitorAdapter() {

        @Override
        public boolean visit(SQLVariantRefExpr x) {
            ++variantIndex;
            return true;
        }

        @Override
        public boolean visit(SQLBinaryOpExpr x) {
            if (hasSecuretField(x.getLeft())) {
                checkOnlyOneAsk(x.getRight());
            } else if (hasSecuretField(x.getRight())) {
                checkOnlyOneAsk(x.getLeft());
            }

            return true;
        }


        private boolean hasSecuretField(SQLExpr field) {
            return field instanceof SQLIdentifierExpr && isSecuretField((SQLIdentifierExpr) field)
                    || field instanceof SQLPropertyExpr && isSecuretField((SQLPropertyExpr) field);
        }
    };

    // TIPS PART FORMAT: /*** bind(1,2,3) result(1) ***/
    private static Pattern encryptHint = Pattern.compile("\\s*/\\*{3}\\s*(.*?)\\s*\\*{3}/");

    private static OracleSensitiveFieldsParser tryParseHint(String sql, Set<String> securetFields) {
        OracleSensitiveFieldsParser fieldsParser = null;

        Matcher matcher = encryptHint.matcher(sql);
        if (matcher.find() && matcher.start() == 0) {
            String convertedSql = sql.substring(matcher.end());
            String hint = matcher.group(1);
            fieldsParser = new OracleSensitiveFieldsParser(securetFields, convertedSql);
            fieldsParser.parseHint(hint);
        }

        return fieldsParser;
    }

    public static OracleSensitiveFieldsParser parseOracleSql(String sql, Set<String> securetFields) {
        OracleSensitiveFieldsParser fieldsParser = tryParseHint(sql, securetFields);
        if (fieldsParser == null) {
            SQLStatement sqlStatement = parseSql(sql);
            fieldsParser = new OracleSensitiveFieldsParser(securetFields, sql);
            fieldsParser = parseStatement(fieldsParser, sqlStatement);
        }

        if (fieldsParser == null) return null;
        if (fieldsParser.haveNotSecureFields()) return null;
        return fieldsParser;
    }


    private static OracleSensitiveFieldsParser parseStatement(OracleSensitiveFieldsParser parser, SQLStatement sqlStatement) {
        if (sqlStatement instanceof SQLSelectStatement) {
            parser.parseSelectQuery(((SQLSelectStatement) sqlStatement).getSelect().getQuery());
        } else if (sqlStatement instanceof OracleDeleteStatement) {
            parser.parseDelete((OracleDeleteStatement) sqlStatement);
        } else if (sqlStatement instanceof OracleInsertStatement) {
            parser.parseInsert((OracleInsertStatement) sqlStatement);
        } else if (sqlStatement instanceof OracleUpdateStatement) {
            parser.parseUpdate((OracleUpdateStatement) sqlStatement);
        } else if (sqlStatement instanceof OracleMergeStatement) {
            parser.parseMerge((OracleMergeStatement) sqlStatement);
        } else if (sqlStatement instanceof SQLCallStatement) {
            parser.parseCall((SQLCallStatement) sqlStatement);
        } else if (sqlStatement instanceof OracleMultiInsertStatement) {
            parser.parseMultiInsert((OracleMultiInsertStatement) sqlStatement);
        }

        return parser;
    }

    private void parseSelectQuery(SQLSelectQuery query) {
        if (query instanceof SQLSelectQueryBlock) {
            parseQuery((SQLSelectQueryBlock) query);
        } else if (query instanceof SQLUnionQuery) {
            parseUnionQuery((SQLUnionQuery) query);
        }
    }

    private static SQLStatement parseSql(String sql) {
        SQLStatementParser parser = new OracleStatementParser(sql);
        List<SQLStatement> stmtList;
        try {
            stmtList = parser.parseStatementList();
        } catch (ParserException exception) {
            exception.printStackTrace();
            throw new RuntimeException(sql + " is invalid, detail " + exception.getMessage());
        }

        return stmtList.get(0);
    }

    private static Pattern bindPattern = Pattern.compile("bind\\s*\\((.*?)\\)");
    private static Pattern resultPattern = Pattern.compile("result\\s*\\((.*?)\\)");
    private static Splitter indexSplitter = Splitter.on(',').omitEmptyStrings().trimResults();


    private OracleSensitiveFieldsParser(Set<String> securetFields, String sql) {
        this.securetFields = securetFields;
        this.sql = sql;
    }

    private void parseHint(String hint) {
        Matcher matcher = bindPattern.matcher(hint);
        if (matcher.find()) {
            Iterable<String> bindIndices = indexSplitter.split(matcher.group(1));
            for (String bindIndex : bindIndices)
                securetBindIndice.add(Integer.parseInt(bindIndex));
        }

        matcher = resultPattern.matcher(hint);
        if (matcher.find()) {
            Iterable<String> resultIndices = indexSplitter.split(matcher.group(1));
            for (String resultIndex : resultIndices)
                securetResultIndice.add(Integer.parseInt(resultIndex));
        }
    }

    private void parseUnionQuery(SQLUnionQuery sqlUnionQuery) {
        SQLSelectQuery left = sqlUnionQuery.getLeft();
        parseQuery((SQLSelectQueryBlock) left);

        SQLSelectQuery right = sqlUnionQuery.getRight();
        if (right instanceof SQLUnionQuery) {
            parseUnionQuery((SQLUnionQuery) right);
        } else {
            parseQuery((SQLSelectQueryBlock) right);
        }
    }

    private void parseQuery(SQLSelectQueryBlock queryBlock) {
        parseTable(queryBlock.getFrom());
        parseSelectItems(queryBlock.getSelectList());
        adjustSubQeuryBindIndiceOfFrom();
        if (queryBlock.getWhere() != null) queryBlock.getWhere().accept(adapter);
    }

    private void adjustSubQeuryBindIndiceOfFrom() {
        for (BindVariant bindVariant : this.subQueryBindAndVariantOfFrom) {
            for (Integer index : bindVariant.getBindIndice())
                this.securetBindIndice.add(this.variantIndex + index);
            this.variantIndex += bindVariant.getVariantIndex();
        }
    }

    private void parseDelete(OracleDeleteStatement deleteStatement) {
        SQLExprTableSource tableSource = (SQLExprTableSource) deleteStatement.getTableSource();
        if (tableSource.getExpr() instanceof SQLIdentifierExpr)
            addTableAlias(tableSource, (SQLIdentifierExpr) tableSource.getExpr());

        if (deleteStatement.getWhere() != null) deleteStatement.getWhere().accept(adapter);
    }

    private void parseCall(SQLCallStatement callStatement) {
        addTableAlias("", callStatement.getProcedureName().toString());
        boolean isOraFunc = callStatement.getOutParameter() != null;
        if (isOraFunc && isSecuretField(1)) securetBindIndice.add(1);

        List<SQLExpr> parameters = callStatement.getParameters();
        for (int i = 0, ii = parameters.size(); i < ii; ++i) {
            SQLExpr parameter = parameters.get(i);
            parameter.accept(adapter);
            int paramIndex = i + 1 + (isOraFunc ? 1 : 0);

            if (!isSecuretField(paramIndex)) continue;
            if (parameter instanceof SQLVariantRefExpr) {
                securetBindIndice.add(variantIndex + (isOraFunc ? 1 : 0));
            } else {
                log.warn("securet field is not passed as a single value in sql [" + sql + "]");
            }
        }
    }

    private void parseMerge(OracleMergeStatement mergeStatement) {
        if (mergeStatement.getInto() instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr expr = (SQLIdentifierExpr) mergeStatement.getInto();
            addTableAlias(mergeStatement.getAlias(), expr);
        }

        mergeStatement.getOn().accept(adapter);

        OracleMergeStatement.MergeUpdateClause updateClause = mergeStatement.getUpdateClause();
        if (updateClause != null) {
            List<SQLUpdateSetItem> items = updateClause.getItems();
            walkUpdateItems(items);
        }

        OracleMergeStatement.MergeInsertClause insertClause = mergeStatement.getInsertClause();
        if (insertClause != null) {
            List<Integer> securetFieldsIndice = walkInsertColumns(insertClause.getColumns());
            walkInsertValues(securetFieldsIndice, insertClause.getValues());
        }
    }

    private void parseUpdate(OracleUpdateStatement updateStatement) {
        OracleSelectTableReference tableSource = (OracleSelectTableReference) updateStatement.getTableSource();
        if (tableSource.getExpr() instanceof SQLIdentifierExpr)
            addTableAlias(tableSource, (SQLIdentifierExpr) tableSource.getExpr());

        List<SQLUpdateSetItem> items = updateStatement.getItems();
        SQLUpdateSetItem item0 = items.get(0);
        if (items.size() == 1 && item0.getColumn() instanceof SQLListExpr
                && item0.getValue() instanceof SQLQueryExpr) {
            // update xxx set (a,b) = (select ... from) where
            walkUpdateSelect(item0);
        } else {
            walkUpdateItems(items);
        }

        if (updateStatement.getWhere() != null) updateStatement.getWhere().accept(adapter);
    }

    private void walkUpdateSelect(SQLUpdateSetItem item) {
        SQLListExpr sqlListExpr = (SQLListExpr) item.getColumn();
        List<SQLExpr> items = sqlListExpr.getItems();
        Set<Integer> securiteFieldIndice = Sets.newHashSet();
        for (int i = 0, ii = items.size(); i < ii; ++i) {
            SQLExpr expr = items.get(i);
            if (expr instanceof SQLPropertyExpr && isSecuretField((SQLPropertyExpr) expr)) {
                securiteFieldIndice.add(i);
            }
        }

        SQLQueryExpr value = (SQLQueryExpr) item.getValue();

        SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) value.getSubQuery().getQuery();

        parseTable(queryBlock.getFrom());
        parseSelectItemsInUpdate(securiteFieldIndice, queryBlock.getSelectList());

        if (queryBlock.getWhere() != null) queryBlock.getWhere().accept(adapter);

    }

    private void parseSelectItemsInUpdate(Set<Integer> securetFieldIndice, List<SQLSelectItem> selectList) {
        for (int i = 0, ii = selectList.size(); i < ii; ++i) {
            SQLSelectItem item = selectList.get(i);
            item.accept(adapter);
            if (securetFieldIndice.contains(i) && item.getExpr() instanceof SQLVariantRefExpr) {
                securetBindIndice.add(variantIndex);
            }
        }
    }

    private void walkUpdateItems(List<SQLUpdateSetItem> items) {
        for (int i = 0, ii = items.size(); i < ii; ++i) {
            SQLUpdateSetItem item = items.get(i);
            item.accept(adapter);

            boolean isSecuretField = false;
            if (item.getColumn() instanceof SQLPropertyExpr) {
                SQLPropertyExpr expr = (SQLPropertyExpr) item.getColumn();
                isSecuretField = isSecuretField(expr);
            } else if (item.getColumn() instanceof SQLIdentifierExpr) {
                isSecuretField = isSecuretField((SQLIdentifierExpr) item.getColumn());
            }

            if (!isSecuretField) continue;

            if (item.getValue() instanceof SQLVariantRefExpr) {
                securetBindIndice.add(variantIndex);
            } else {
                log.warn("securet field is not updated as a single value in sql [" + sql + "]");
            }
        }
    }


    // only check one situation of right ? like: A.PCARD_CODE = upper(?)
    private void checkOnlyOneAsk(SQLExpr right) {
        final AtomicInteger rightVariantIndex = new AtomicInteger(0);
        right.accept(new OracleASTVisitorAdapter() {
            @Override
            public boolean visit(SQLVariantRefExpr x) {
                rightVariantIndex.incrementAndGet();
                return true;
            }
        });

        if (rightVariantIndex.get() == 1) securetBindIndice.add(variantIndex + 1);
    }


    private boolean isSecuretField(SQLAllColumnExpr field) {
        Object oneTableName = getOneTableName();
        return oneTableName != null && containsInSecuretFields(oneTableName, "*");
    }

    private boolean isSecuretField(SQLIdentifierExpr field) {
        Object oneTableName = getOneTableName();
        return oneTableName != null && containsInSecuretFields(oneTableName, field.getName());
    }

    private boolean isSecuretField(SQLPropertyExpr expr) {
        Object tableName = aliasTablesMap.get(expr.getOwner().toString());
        String fieldName = expr.getName();


        return containsInSecuretFields(tableName, fieldName);
    }

    private boolean containsInSecuretFields(Object tableName, String fieldName) {
        if (tableName instanceof String)
            return containsInSecuretFields((String) tableName, fieldName);
        else if (tableName instanceof OracleSensitiveFieldsParser)
            return containsInSecuretFields((OracleSensitiveFieldsParser) tableName, fieldName);

        return false;
    }

    private boolean containsInSecuretFields(OracleSensitiveFieldsParser parser, String fieldName) {
        return "*".equals(fieldName)
                ? !parser.getSecuretResultIndice().isEmpty()
                : parser.inResultLables(fieldName);
    }

    private boolean containsInSecuretFields(String tableName, String fieldName) {
        String secretField = tableName + "." + fieldName;
        return securetFields.contains(secretField.toUpperCase());
    }

    private boolean isSecuretField(int procedureParameterIndex) {
        Object oneTableName = getOneTableName();
        return oneTableName != null && containsInSecuretFields(oneTableName, "" + procedureParameterIndex);
    }

    private Object getOneTableName() {
        if (aliasTablesMap.size() == 1)
            for (Map.Entry<String, Object> entry : aliasTablesMap.entrySet())
                return entry.getValue();

        return null;
    }

    private void parseTable(SQLTableSource from) {
        if (from instanceof OracleSelectTableReference) {
            SQLExprTableSource source = (SQLExprTableSource) from;

            if (source.getExpr() instanceof SQLIdentifierExpr)
                addTableAlias(from, (SQLIdentifierExpr) source.getExpr());

        } else if (from instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTableSource = (SQLJoinTableSource) from;
            parseTable(joinTableSource.getLeft());
            parseTable(joinTableSource.getRight());

            // maybe there are binding variants in connection
            SQLExpr conditionOn = joinTableSource.getCondition();
            if (conditionOn != null) conditionOn.accept(adapter);
        } else if (from instanceof OracleSelectSubqueryTableSource) {
            OracleSelectSubqueryTableSource tableSource = (OracleSelectSubqueryTableSource) from;
            SQLSelectQuery query = tableSource.getSelect().getQuery();
            OracleSensitiveFieldsParser subParser = createSubQueryParser(query, QueryBelongs.FROM);
            addTableAlias(from, subParser);
        }
    }

    private void addTableAlias(SQLTableSource from, OracleSensitiveFieldsParser subParser) {
        addTableAlias(from.getAlias(), subParser);
    }


    private void addTableAlias(SQLTableSource from, SQLIdentifierExpr expr) {
        addTableAlias(from.getAlias(), expr);
    }

    private void addTableAlias(String alias, SQLIdentifierExpr expr) {
        addTableAlias(alias, expr.getName());
    }

    private void addTableAlias(String alias, String tableName) {
        aliasTablesMap.put(Objects.firstNonNull(alias, tableName), tableName);
    }


    private void addTableAlias(String alias, OracleSensitiveFieldsParser subParser) {
        aliasTablesMap.put(alias, subParser);
    }

    private String cleanQuotes(String str) {
        if (str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"'
                || str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'')
            return str.substring(1, str.length() - 1);


        return str;
    }

    private void parseSelectItems(List<SQLSelectItem> sqlSelectItems) {
        for (int itemIndex = 0, ii = sqlSelectItems.size(); itemIndex < ii; ++itemIndex) {
            SQLSelectItem item = sqlSelectItems.get(itemIndex);
            String alias = item.getAlias();

            if (item.getExpr() instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr expr = (SQLIdentifierExpr) item.getExpr();

                if (isSecuretField(expr)) {
                    securetResultIndice.add(itemIndex + 1);
                    securetResultLabels.add(cleanQuotes(alias == null ? expr.getName() : alias));
                }

            } else if (item.getExpr() instanceof SQLPropertyExpr) {
                SQLPropertyExpr expr = (SQLPropertyExpr) item.getExpr();
                if (isSecuretField(expr)) {
                    if ("*".equals(expr.getName())) {
                        Object tableName = aliasTablesMap.get(expr.getOwner().toString());
                        copyResultIndiceAndLables(itemIndex, tableName);

                    } else {
                        securetResultIndice.add(itemIndex + 1);
                        securetResultLabels.add(cleanQuotes(alias == null ? expr.getName() : alias));
                    }
                }
            } else if (item.getExpr() instanceof SQLAllColumnExpr) {
                if (isSecuretField((SQLAllColumnExpr) item.getExpr())) {
                    Object tableName = getOneTableName();
                    copyResultIndiceAndLables(itemIndex, tableName);
                }

            } else if (item.getExpr() instanceof SQLQueryExpr) {
                SQLQueryExpr expr = (SQLQueryExpr) item.getExpr();
                SQLSelectQuery subQuery = expr.getSubQuery().getQuery();
                OracleSensitiveFieldsParser subParser = createSubQueryParser(subQuery, QueryBelongs.SELECT);

                if (subParser.inResultIndice(1)) {
                    securetResultIndice.add(itemIndex + 1);
                    Set<String> labels = subParser.getSecuretResultLabels();
                    securetResultLabels.add(cleanQuotes(alias == null ? labels.iterator().next() : alias));
                }
            }
        }
    }

    private void copyResultIndiceAndLables(int itemIndex, Object tableName) {
        if (tableName instanceof OracleSensitiveFieldsParser) {
            OracleSensitiveFieldsParser parser = (OracleSensitiveFieldsParser) tableName;
            for (Integer resultIndex : parser.getSecuretResultIndice()) {
                securetResultIndice.add(resultIndex + itemIndex);
            }
            securetResultLabels.addAll(parser.getSecuretResultLabels());
        }
    }

    private OracleSensitiveFieldsParser createSubQueryParser(SQLSelectQuery subQuery, QueryBelongs mode) {
        OracleSensitiveFieldsParser subParser = new OracleSensitiveFieldsParser(securetFields, sql);
        subParser.parseSelectQuery(subQuery);

        switch (mode) {
            case FROM:
                BindVariant bindAndVariant = new BindVariant(subParser.getVariantIndex(),
                        subParser.getSecuretBindIndice());
                subQueryBindAndVariantOfFrom.add(bindAndVariant);
                break;

            case SELECT:
                for (Integer index : subParser.getSecuretBindIndice())
                    this.securetBindIndice.add(variantIndex + index);
                variantIndex += subParser.getVariantIndex();
                break;
        }

        return subParser;
    }


    private void parseMultiInsert(OracleMultiInsertStatement multiInsertStatement) {
        List<OracleMultiInsertStatement.Entry> entries = multiInsertStatement.getEntries();
        for (OracleMultiInsertStatement.Entry entry : entries) {
            parseInsert((OracleMultiInsertStatement.InsertIntoClause) entry);
        }
    }


    private void parseInsert(SQLInsertInto x) {
        SQLExprTableSource tableSource = x.getTableSource();
        if (tableSource.getExpr() instanceof SQLIdentifierExpr)
            addTableAlias(tableSource, (SQLIdentifierExpr) tableSource.getExpr());

        List<SQLExpr> columns = x.getColumns();
        List<Integer> securetFieldsIndice = walkInsertColumns(columns);

        SQLInsertStatement.ValuesClause valuesClause = x.getValues();
        // may be insert ... select ...
        if (valuesClause != null) {
            List<SQLExpr> values = valuesClause.getValues();
            walkInsertValues(securetFieldsIndice, values);
        } else if (x.getQuery() != null) {
            SQLSelect query = x.getQuery();
            parseQuery4Insert(securetFieldsIndice, (SQLSelectQueryBlock) query.getQuery());
        }
    }

    private void parseQuery4Insert(List<Integer> securetFieldsIndice, SQLSelectQueryBlock queryBlock) {
        List<SQLSelectItem> selectList = queryBlock.getSelectList();
        for (int itemIndex = 0, ii = selectList.size(); itemIndex < ii; ++itemIndex) {
            SQLSelectItem item = selectList.get(itemIndex);
            item.accept(adapter);
            if (securetFieldsIndice.contains(itemIndex) && item.getExpr() instanceof SQLVariantRefExpr) {
                securetBindIndice.add(variantIndex);
            }
        }

        queryBlock.getFrom().accept(adapter);

        parseTable(queryBlock.getFrom());

        if (queryBlock.getWhere() != null) queryBlock.getWhere().accept(adapter);
    }

    private void walkInsertValues(List<Integer> securetFieldsIndice, List<SQLExpr> values) {
        for (int i = 0, ii = values.size(); i < ii; ++i) {
            SQLExpr expr = values.get(i);
            expr.accept(adapter);
            if (securetFieldsIndice.contains(i)) {
                if (expr instanceof SQLVariantRefExpr) securetBindIndice.add(variantIndex);
                else log.warn("securet field is not inserted as a single value in sql [" + sql + "]");
            }
        }
    }

    private List<Integer> walkInsertColumns(List<SQLExpr> columns) {
        List<Integer> securetFieldsIndice = Lists.newArrayList();
        for (int i = 0, ii = columns.size(); i < ii; ++i) {
            SQLExpr column = columns.get(i);
            if (column instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr expr = (SQLIdentifierExpr) column;
                if (isSecuretField(expr)) securetFieldsIndice.add(i);
            }
        }

        return securetFieldsIndice;
    }

    @Override
    public Set<Integer> getSecuretBindIndice() {
        return securetBindIndice;
    }

    @Override
    public Set<Integer> getSecuretResultIndice() {
        return securetResultIndice;
    }

    @Override
    public Set<String> getSecuretResultLabels() {
        return securetResultLabels;
    }

    @Override
    public boolean inBindIndice(int index) {
        return getSecuretBindIndice().contains(index);
    }

    @Override
    public boolean inResultIndice(int index) {
        return getSecuretResultIndice().contains(index);
    }

    @Override
    public boolean inResultLables(String label) {
        return getSecuretResultLabels().contains(label);
    }

    @Override
    public boolean inResultIndiceOrLabel(Object indexOrLabel) {
        return getSecuretResultIndice().contains(indexOrLabel)
                || getSecuretResultLabels().contains(indexOrLabel);
    }

    @Override
    public boolean haveNotSecureFields() {
        return securetResultLabels.isEmpty()
                && securetResultIndice.isEmpty()
                && securetBindIndice.isEmpty();
    }

    @Override
    public String getSql() {
        return sql;
    }

    public int getVariantIndex() {
        return variantIndex;
    }

    static enum QueryBelongs {
        FROM, SELECT
    }


    static class BindVariant {
        private Integer variantIndex;
        private Set<Integer> bindIndice;

        public BindVariant(Integer variantIndex, Set<Integer> bindIndice) {
            this.variantIndex = variantIndex;
            this.bindIndice = bindIndice;
        }

        public Integer getVariantIndex() {
            return variantIndex;
        }

        public Set<Integer> getBindIndice() {
            return bindIndice;
        }
    }

}
