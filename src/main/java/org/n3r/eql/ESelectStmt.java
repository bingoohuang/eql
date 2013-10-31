package org.n3r.eql;

import org.n3r.eql.impl.EqlRsRetriever;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.param.EqlParamsBinder;
import org.n3r.eql.map.EqlRun;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ESelectStmt implements Closeable, EStmt {
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private boolean resultSetNext;
    private EqlRsRetriever rsRetriever;
    private int rowNum;
    private EqlRun eqlRun;
    private Logger logger;
    private Object[] params;
    private int fetchSize;

    public void executeQuery() {
        executeQuery(params);
    }

    public void executeQuery(Object... params) {
        resultSetNext = true;
        rowNum = 0;
        try {
            new EqlParamsBinder().bindParams(preparedStatement, eqlRun, params, logger);
            resultSet = preparedStatement.executeQuery();
            if (fetchSize > 0) resultSet.setFetchSize(fetchSize);
        } catch (SQLException e) {
            throw new EqlExecuteException("executeQuery", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T next() {
        if (!resultSetNext)
            return null;

        try {
            T rowBean = (T) rsRetriever.selectRow(resultSet, ++rowNum);
            if (rowBean == null) {
                resultSetNext = false;
                closeRs();
            }
            return rowBean;
        } catch (SQLException e) {
            throw new EqlExecuteException("select row", e);
        }
    }

    public void closeRs() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception e) {
                // ignore
            }
        }
        resultSet = null;
    }

    @Override
    public void closeStmt() {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (Exception e) {
                // Ignore
            }
        }
        preparedStatement = null;
    }

    @Override
    public void setPreparedStatment(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public void setRsRetriever(EqlRsRetriever rsRetriever) {
        this.rsRetriever = rsRetriever;
    }

    @Override
    public void close() throws IOException {
        closeRs();
        closeStmt();
    }

    @Override
    public void setEqlRun(EqlRun eqlRun) {
        this.eqlRun = eqlRun;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setEqlTran(EqlTran eqlTran) {
    }

    @Override
    public void setParams(Object[] params) {
        this.params = params;
    }

    @Override
    public Object[] getParams() {
        return this.params;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
}
