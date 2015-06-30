package org.n3r.eql.dbfieldcryptor.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlReplaceStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
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

public class MySqlSensitiveFieldsParser implements SensitiveFieldsParser {
    private final Logger log = LoggerFactory.getLogger(MySqlSensitiveFieldsParser.class);

    private final Map<String, Object> aliasTablesMap = Maps.newHashMap();
    private final Set<Integer> secureBindIndices = Sets.newHashSet();
    private final Set<Integer> secureResultIndices = Sets.newHashSet();
    private final Set<String> secureResultLabels = Sets.newHashSet();

    private final List<BindVariant> subQueryBindAndVariantOfFrom = Lists.newArrayList();

    private final Set<String> secureFields;

    private int variantIndex = 0;
    private final String sql;

    private MySqlASTVisitorAdapter adapter = new MySqlASTVisitorAdapter() {

        @Override
        public boolean visit(SQLVariantRefExpr x) {
            ++variantIndex;
            return true;
        }

        @Override
        public boolean visit(SQLBinaryOpExpr x) {
            if (hasSecureField(x.getLeft())) {
                checkOnlyOneAsk(x.getRight());
            } else if (hasSecureField(x.getRight())) {
                checkOnlyOneAsk(x.getLeft());
            }

            return true;
        }


        private boolean hasSecureField(SQLExpr field) {
            return field instanceof SQLIdentifierExpr && isSecureField((SQLIdentifierExpr) field)
                    || field instanceof SQLPropertyExpr && isSecureField((SQLPropertyExpr) field);
        }
    };

    // TIPS PART FORMAT: /*** bind(1,2,3) result(1) ***/
    private static Pattern encryptHint = Pattern.compile("\\s*/\\*{3}\\s*(.*?)\\s*\\*{3}/");

    private static MySqlSensitiveFieldsParser tryParseHint(String sql, Set<String> secureFields) {
        MySqlSensitiveFieldsParser fieldsParser = null;

        Matcher matcher = encryptHint.matcher(sql);
        if (matcher.find() && matcher.start() == 0) {
            String convertedSql = sql.substring(matcher.end());
            String hint = matcher.group(1);
            fieldsParser = new MySqlSensitiveFieldsParser(secureFields, convertedSql);
            fieldsParser.parseHint(hint);
        }

        return fieldsParser;
    }

    public static MySqlSensitiveFieldsParser parseSql(String sql, Set<String> secureFields) {
        MySqlSensitiveFieldsParser fieldsParser = tryParseHint(sql, secureFields);
        if (fieldsParser == null) {
            SQLStatement sqlStatement = parseSql(sql);
            fieldsParser = new MySqlSensitiveFieldsParser(secureFields, sql);
            fieldsParser = parseStatement(fieldsParser, sqlStatement);
        }

        if (fieldsParser == null) return null;
        if (fieldsParser.haveNonSecureFields()) return null;
        return fieldsParser;
    }


    private static MySqlSensitiveFieldsParser parseStatement(MySqlSensitiveFieldsParser parser, SQLStatement sqlStatement) {
        if (sqlStatement instanceof SQLSelectStatement) {
            parser.parseSelectQuery(((SQLSelectStatement) sqlStatement).getSelect().getQuery());
        } else if (sqlStatement instanceof MySqlDeleteStatement) {
            parser.parseDelete((MySqlDeleteStatement) sqlStatement);
        } else if (sqlStatement instanceof MySqlInsertStatement) {
            parser.parseInsert((MySqlInsertStatement) sqlStatement);
        } else if (sqlStatement instanceof MySqlReplaceStatement) {
            parser.parseReplace((MySqlReplaceStatement) sqlStatement);
        } else if (sqlStatement instanceof MySqlUpdateStatement) {
            parser.parseUpdate((MySqlUpdateStatement) sqlStatement);
        } else if (sqlStatement instanceof SQLCallStatement) {
            parser.parseCall((SQLCallStatement) sqlStatement);
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
        SQLStatementParser parser = new MySqlStatementParser(sql);
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


    private MySqlSensitiveFieldsParser(Set<String> secureFields, String sql) {
        this.secureFields = secureFields;
        this.sql = sql;
    }

    private void parseHint(String hint) {
        Matcher matcher = bindPattern.matcher(hint);
        if (matcher.find()) {
            Iterable<String> bindIndices = indexSplitter.split(matcher.group(1));
            for (String bindIndex : bindIndices)
                secureBindIndices.add(Integer.parseInt(bindIndex));
        }

        matcher = resultPattern.matcher(hint);
        if (matcher.find()) {
            Iterable<String> resultIndices = indexSplitter.split(matcher.group(1));
            for (String resultIndex : resultIndices)
                secureResultIndices.add(Integer.parseInt(resultIndex));
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
        adjustSubQueryBindIndicesOfFrom();
        if (queryBlock.getWhere() != null) queryBlock.getWhere().accept(adapter);
    }

    private void adjustSubQueryBindIndicesOfFrom() {
        for (BindVariant bindVariant : this.subQueryBindAndVariantOfFrom) {
            for (Integer index : bindVariant.getBindIndices())
                this.secureBindIndices.add(this.variantIndex + index);
            this.variantIndex += bindVariant.getVariantIndex();
        }
    }

    private void parseDelete(MySqlDeleteStatement deleteStatement) {
        SQLExprTableSource tableSource = (SQLExprTableSource) deleteStatement.getTableSource();
        if (tableSource.getExpr() instanceof SQLIdentifierExpr)
            addTableAlias(tableSource, (SQLIdentifierExpr) tableSource.getExpr());

        if (deleteStatement.getWhere() != null) deleteStatement.getWhere().accept(adapter);
    }

    private void parseCall(SQLCallStatement callStatement) {
        addTableAlias("", callStatement.getProcedureName().toString());
        boolean isOraFunc = callStatement.getOutParameter() != null;
        if (isOraFunc && isSecureField(1)) secureBindIndices.add(1);

        List<SQLExpr> parameters = callStatement.getParameters();
        for (int i = 0, ii = parameters.size(); i < ii; ++i) {
            SQLExpr parameter = parameters.get(i);
            parameter.accept(adapter);
            int paramIndex = i + 1 + (isOraFunc ? 1 : 0);

            if (!isSecureField(paramIndex)) continue;
            if (parameter instanceof SQLVariantRefExpr) {
                secureBindIndices.add(variantIndex + (isOraFunc ? 1 : 0));
            } else {
                log.warn("secure field is not passed as a single value in sql [" + sql + "]");
            }
        }
    }

    private void parseUpdate(MySqlUpdateStatement updateStatement) {
        SQLTableSource tableSource = updateStatement.getTableSource();
        if (tableSource instanceof SQLExprTableSource) {
            SQLExprTableSource ets = (SQLExprTableSource) tableSource;
            addTableAlias(ets.getAlias(), (SQLIdentifierExpr) ets.getExpr());
        }

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
        Set<Integer> secureFieldIndices = Sets.newHashSet();
        for (int i = 0, ii = items.size(); i < ii; ++i) {
            SQLExpr expr = items.get(i);
            if (expr instanceof SQLPropertyExpr && isSecureField((SQLPropertyExpr) expr)) {
                secureFieldIndices.add(i);
            }
        }

        SQLQueryExpr value = (SQLQueryExpr) item.getValue();

        SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) value.getSubQuery().getQuery();

        parseTable(queryBlock.getFrom());
        parseSelectItemsInUpdate(secureFieldIndices, queryBlock.getSelectList());

        if (queryBlock.getWhere() != null) queryBlock.getWhere().accept(adapter);

    }

    private void parseSelectItemsInUpdate(Set<Integer> secureFieldIndices, List<SQLSelectItem> selectList) {
        for (int i = 0, ii = selectList.size(); i < ii; ++i) {
            SQLSelectItem item = selectList.get(i);
            item.accept(adapter);
            if (secureFieldIndices.contains(i) && item.getExpr() instanceof SQLVariantRefExpr) {
                secureBindIndices.add(variantIndex);
            }
        }
    }

    private void walkUpdateItems(List<SQLUpdateSetItem> items) {
        for (int i = 0, ii = items.size(); i < ii; ++i) {
            SQLUpdateSetItem item = items.get(i);
            item.accept(adapter);

            boolean isSecureField = false;
            if (item.getColumn() instanceof SQLPropertyExpr) {
                SQLPropertyExpr expr = (SQLPropertyExpr) item.getColumn();
                isSecureField = isSecureField(expr);
            } else if (item.getColumn() instanceof SQLIdentifierExpr) {
                isSecureField = isSecureField((SQLIdentifierExpr) item.getColumn());
            }

            if (!isSecureField) continue;

            if (item.getValue() instanceof SQLVariantRefExpr) {
                secureBindIndices.add(variantIndex);
            } else {
                log.warn("secure field is not updated as a single value in sql [" + sql + "]");
            }
        }
    }


    // only check one situation of right ? like: A.PCARD_CODE = upper(?)
    private void checkOnlyOneAsk(SQLExpr right) {
        final AtomicInteger rightVariantIndex = new AtomicInteger(0);
        right.accept(new MySqlASTVisitorAdapter() {
            @Override
            public boolean visit(SQLVariantRefExpr x) {
                rightVariantIndex.incrementAndGet();
                return true;
            }
        });

        if (rightVariantIndex.get() == 1) secureBindIndices.add(variantIndex + 1);
    }


    private boolean isSecureField(SQLAllColumnExpr field) {
        Object oneTableName = getOneTableName();
        return oneTableName != null && containsInSecureFields(oneTableName, "*");
    }

    private boolean isSecureField(SQLIdentifierExpr field) {
        Object oneTableName = getOneTableName();
        return oneTableName != null && containsInSecureFields(oneTableName, field.getName());
    }

    private boolean isSecureField(SQLPropertyExpr expr) {
        Object tableName = aliasTablesMap.get(expr.getOwner().toString());
        String fieldName = expr.getName();


        return containsInSecureFields(tableName, fieldName);
    }

    private boolean containsInSecureFields(Object tableName, String fieldName) {
        if (tableName instanceof String)
            return containsInSecureFields((String) tableName, fieldName);
        else if (tableName instanceof MySqlSensitiveFieldsParser)
            return containsInSecureFields((MySqlSensitiveFieldsParser) tableName, fieldName);

        return false;
    }

    private boolean containsInSecureFields(MySqlSensitiveFieldsParser parser, String fieldName) {
        return "*".equals(fieldName)
                ? !parser.getSecureResultIndices().isEmpty()
                : parser.inResultLabels(fieldName);
    }

    private boolean containsInSecureFields(String tableName, String fieldName) {
        String secretField = tableName + "." + fieldName;
        return secureFields.contains(secretField.toUpperCase());
    }

    private boolean isSecureField(int procedureParameterIndex) {
        Object oneTableName = getOneTableName();
        return oneTableName != null && containsInSecureFields(oneTableName, "" + procedureParameterIndex);
    }

    private Object getOneTableName() {
        if (aliasTablesMap.size() == 1)
            for (Map.Entry<String, Object> entry : aliasTablesMap.entrySet())
                return entry.getValue();

        return null;
    }

    private void parseTable(SQLTableSource from) {
        if (from instanceof SQLExprTableSource) {
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
        } else if (from instanceof SQLSubqueryTableSource) {
            SQLSubqueryTableSource tableSource = (SQLSubqueryTableSource) from;
            SQLSelectQuery query = tableSource.getSelect().getQuery();
            MySqlSensitiveFieldsParser subParser = createSubQueryParser(query, QueryBelongs.FROM);
            addTableAlias(from, subParser);
        }
    }

    private void addTableAlias(SQLTableSource from, MySqlSensitiveFieldsParser subParser) {
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


    private void addTableAlias(String alias, MySqlSensitiveFieldsParser subParser) {
        aliasTablesMap.put(alias, subParser);
    }

    private String cleanQuotesAndToUpper(String str) {
        String cleanString = str;
        if (str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"'
                || str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'')
            cleanString = str.substring(1, str.length() - 1);


        return cleanString.toUpperCase();
    }

    private void parseSelectItems(List<SQLSelectItem> sqlSelectItems) {
        for (int itemIndex = 0, ii = sqlSelectItems.size(); itemIndex < ii; ++itemIndex) {
            SQLSelectItem item = sqlSelectItems.get(itemIndex);
            String alias = item.getAlias();

            if (item.getExpr() instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr expr = (SQLIdentifierExpr) item.getExpr();

                if (isSecureField(expr)) {
                    secureResultIndices.add(itemIndex + 1);
                    secureResultLabels.add(cleanQuotesAndToUpper(alias == null ? expr.getName() : alias));
                }

            } else if (item.getExpr() instanceof SQLPropertyExpr) {
                SQLPropertyExpr expr = (SQLPropertyExpr) item.getExpr();
                if (isSecureField(expr)) {
                    if ("*".equals(expr.getName())) {
                        Object tableName = aliasTablesMap.get(expr.getOwner().toString());
                        copyResultIndicesAndLabels(itemIndex, tableName);

                    } else {
                        secureResultIndices.add(itemIndex + 1);
                        secureResultLabels.add(cleanQuotesAndToUpper(alias == null ? expr.getName() : alias));
                    }
                }
            } else if (item.getExpr() instanceof SQLAllColumnExpr) {
                if (isSecureField((SQLAllColumnExpr) item.getExpr())) {
                    Object tableName = getOneTableName();
                    copyResultIndicesAndLabels(itemIndex, tableName);
                }

            } else if (item.getExpr() instanceof SQLQueryExpr) {
                SQLQueryExpr expr = (SQLQueryExpr) item.getExpr();
                SQLSelectQuery subQuery = expr.getSubQuery().getQuery();
                MySqlSensitiveFieldsParser subParser = createSubQueryParser(subQuery, QueryBelongs.SELECT);

                if (subParser.inResultIndices(1)) {
                    secureResultIndices.add(itemIndex + 1);
                    Set<String> labels = subParser.getSecureResultLabels();
                    secureResultLabels.add(cleanQuotesAndToUpper(alias == null ? labels.iterator().next() : alias));
                }
            }
        }
    }

    private void copyResultIndicesAndLabels(int itemIndex, Object tableName) {
        if (tableName instanceof MySqlSensitiveFieldsParser) {
            MySqlSensitiveFieldsParser parser = (MySqlSensitiveFieldsParser) tableName;
            for (Integer resultIndex : parser.getSecureResultIndices()) {
                secureResultIndices.add(resultIndex + itemIndex);
            }
            secureResultLabels.addAll(parser.getSecureResultLabels());
        }
    }

    private MySqlSensitiveFieldsParser createSubQueryParser(SQLSelectQuery subQuery, QueryBelongs mode) {
        MySqlSensitiveFieldsParser subParser = new MySqlSensitiveFieldsParser(secureFields, sql);
        subParser.parseSelectQuery(subQuery);

        switch (mode) {
            case FROM:
                BindVariant bindAndVariant = new BindVariant(subParser.getVariantIndex(),
                        subParser.getSecureBindIndices());
                subQueryBindAndVariantOfFrom.add(bindAndVariant);
                break;

            case SELECT:
                for (Integer index : subParser.getSecureBindIndices())
                    this.secureBindIndices.add(variantIndex + index);
                variantIndex += subParser.getVariantIndex();
                break;
        }

        return subParser;
    }

    private void parseReplace(MySqlReplaceStatement x) {
        SQLExprTableSource tableSource = x.getTableSource();
        if (tableSource.getExpr() instanceof SQLIdentifierExpr)
            addTableAlias(tableSource, (SQLIdentifierExpr) tableSource.getExpr());

        List<SQLExpr> columns = x.getColumns();
        List<Integer> secureFieldsIndices = walkInsertColumns(columns);

        List<SQLInsertStatement.ValuesClause> valuesList = x.getValuesList();

        // may be insert ... select ...
        if (valuesList != null) {
            for (SQLInsertStatement.ValuesClause valuesClause : valuesList) {
                walkInsertValues(secureFieldsIndices, valuesClause.getValues());
            }
        } else if (x.getQuery() != null) {
            SQLQueryExpr query = x.getQuery();
//            parseQuery4Insert(secureFieldsIndices, (SQLSelectQueryBlock) query.gets());
        }
    }

    private void parseInsert(SQLInsertInto x) {
        SQLExprTableSource tableSource = x.getTableSource();
        if (tableSource.getExpr() instanceof SQLIdentifierExpr)
            addTableAlias(tableSource, (SQLIdentifierExpr) tableSource.getExpr());

        List<SQLExpr> columns = x.getColumns();
        List<Integer> secureFieldsIndices = walkInsertColumns(columns);

        SQLInsertStatement.ValuesClause valuesClause = x.getValues();
        // may be insert ... select ...
        if (valuesClause != null) {
            List<SQLExpr> values = valuesClause.getValues();
            walkInsertValues(secureFieldsIndices, values);
        } else if (x.getQuery() != null) {
            SQLSelect query = x.getQuery();
            parseQuery4Insert(secureFieldsIndices, (SQLSelectQueryBlock) query.getQuery());
        }
    }

    private void parseQuery4Insert(List<Integer> secureFieldsIndices, SQLSelectQueryBlock queryBlock) {
        List<SQLSelectItem> selectList = queryBlock.getSelectList();
        for (int itemIndex = 0, ii = selectList.size(); itemIndex < ii; ++itemIndex) {
            SQLSelectItem item = selectList.get(itemIndex);
            item.accept(adapter);
            if (secureFieldsIndices.contains(itemIndex) && item.getExpr() instanceof SQLVariantRefExpr) {
                secureBindIndices.add(variantIndex);
            }
        }

        queryBlock.getFrom().accept(adapter);

        parseTable(queryBlock.getFrom());

        if (queryBlock.getWhere() != null) queryBlock.getWhere().accept(adapter);
    }

    private void walkInsertValues(List<Integer> secureFieldsIndices, List<SQLExpr> values) {
        for (int i = 0, ii = values.size(); i < ii; ++i) {
            SQLExpr expr = values.get(i);
            expr.accept(adapter);
            if (secureFieldsIndices.contains(i)) {
                if (expr instanceof SQLVariantRefExpr) secureBindIndices.add(variantIndex);
                else log.warn("secure field is not inserted as a single value in sql [" + sql + "]");
            }
        }
    }

    private List<Integer> walkInsertColumns(List<SQLExpr> columns) {
        List<Integer> secureFieldsIndices = Lists.newArrayList();
        for (int i = 0, ii = columns.size(); i < ii; ++i) {
            SQLExpr column = columns.get(i);
            if (column instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr expr = (SQLIdentifierExpr) column;
                if (isSecureField(expr)) secureFieldsIndices.add(i);
            }
        }

        return secureFieldsIndices;
    }

    @Override
    public Set<Integer> getSecureBindIndices() {
        return secureBindIndices;
    }

    @Override
    public Set<Integer> getSecureResultIndices() {
        return secureResultIndices;
    }

    @Override
    public Set<String> getSecureResultLabels() {
        return secureResultLabels;
    }

    @Override
    public boolean inBindIndices(int index) {
        return getSecureBindIndices().contains(index);
    }

    @Override
    public boolean inResultIndices(int index) {
        return getSecureResultIndices().contains(index);
    }

    @Override
    public boolean inResultLabels(String label) {
        return getSecureResultLabels().contains(label);
    }

    @Override
    public boolean inResultIndicesOrLabel(Object indexOrLabel) {
        if (indexOrLabel instanceof Number) {
            return getSecureResultIndices().contains(indexOrLabel);
        } else if (indexOrLabel instanceof String) {
            String upper = ((String) indexOrLabel).toUpperCase();
            return getSecureResultLabels().contains(upper);
        }

        return false;
    }

    @Override
    public boolean haveNonSecureFields() {
        return secureResultLabels.isEmpty()
                && secureResultIndices.isEmpty()
                && secureBindIndices.isEmpty();
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
        private Set<Integer> bindIndices;

        public BindVariant(Integer variantIndex, Set<Integer> bindIndices) {
            this.variantIndex = variantIndex;
            this.bindIndices = bindIndices;
        }

        public Integer getVariantIndex() {
            return variantIndex;
        }

        public Set<Integer> getBindIndices() {
            return bindIndices;
        }
    }

}
