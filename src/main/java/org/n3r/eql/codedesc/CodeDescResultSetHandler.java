package org.n3r.eql.codedesc;

import lombok.SneakyThrows;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.O;
import org.n3r.eql.util.Rs;
import org.n3r.eql.util.S;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeDescResultSetHandler implements InvocationHandler {
    final List<CodeDesc> codeDescs;
    final EqlConfigDecorator eqlConfig;
    final String sqlClassPath;
    final EqlRun currEqlRun;
    final ResultSet resultSet;
    final Map<String, Integer> codeIndex = new HashMap<String, Integer>();
    private String tagSqlId;

    public CodeDescResultSetHandler(EqlRun currEqlRun, EqlConfigDecorator eqlConfig, String sqlClassPath, ResultSet resultSet, List<CodeDesc> codeDescs, String tagSqlId) {
        this.currEqlRun = currEqlRun;
        this.eqlConfig = eqlConfig;
        this.sqlClassPath = sqlClassPath;
        this.resultSet = resultSet;
        this.codeDescs = codeDescs;
        this.tagSqlId = tagSqlId;

        createCodeIndex();
    }

    private void createCodeIndex() {
        for (CodeDesc codeDesc : codeDescs) {
            codeIndex.put(codeDesc.getColumnName(), findCodeIndex(codeDesc.getColumnName()));
        }
    }

    @SneakyThrows
    private int findCodeIndex(String columnName) {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; ++i) { // first find the desc column index
            String lookupColumnName = Rs.lookupColumnName(metaData, i);
            if (S.equalsIgnoreCase(columnName, lookupColumnName)) {
                for (int j = i - 1; j >= 1; --j) {
                    // then back find the first column(not desc column) to view as code
                    String jColumnName = Rs.lookupColumnName(metaData, j);
                    if (!codeDescsContainsColumnName(jColumnName)) return j;
                }
            }
        }

        throw new EqlExecuteException("unable to find code column");
    }

    private boolean codeDescsContainsColumnName(String jColumnName) {
        for (CodeDesc codeDesc : codeDescs) {
            if (codeDesc.getColumnName().equalsIgnoreCase(jColumnName))
                return true;
        }
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(resultSet, args);

        if (!O.in(method.getName(), "getString", "getObject")) return result;

        String columnName = parseArgName(args[0]);
        if (columnName == null) return result;

        CodeDesc codeDesc = findCodeDesc(columnName);
        if (codeDesc == null) return result;

        String code = resultSet.getString(codeIndex.get(codeDesc.getColumnName()));
        return CodeDescs.map(currEqlRun, eqlConfig, sqlClassPath, codeDesc, code, tagSqlId);
    }

    private CodeDesc findCodeDesc(String columnName) {
        for (CodeDesc codeDesc : codeDescs) {
            if (S.equalsIgnoreCase(columnName, codeDesc.getColumnName())) {
                return codeDesc;
            }
        }

        return null;
    }

    private String parseArgName(Object arg) throws SQLException {
        if (arg instanceof String) return (String) arg;

        if (arg instanceof Integer) {
            Integer index = (Integer) arg;
            return Rs.lookupColumnName(resultSet.getMetaData(), index);
        }

        return null;
    }

    public ResultSet createProxy() {
        return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ResultSet.class}, this);
    }
}
