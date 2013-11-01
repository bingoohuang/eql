package org.n3r.eql.trans;

import org.n3r.eql.EqlTran;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.util.EqlUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class EqlJdbcTransaction implements EqlTran {
    private Connection connection;

    public EqlJdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void start() {
        try {
            if (connection == null) throw new EqlExecuteException(
                    "EqlJdbcTransaction could not start transaction. " +
                            " Cause: The DataSource returned a null connection.");

            if (connection.getAutoCommit()) connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }
    }

    @Override
    public void commit() {
        if (connection == null) return;

        try {
            connection.commit();
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }
    }

    @Override
    public void rollback() {
        if (connection == null) return;

        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }
    }

    @Override
    public Connection getConn() {
        return connection;
    }

    /**
     * Oracle JDBC会在close时自动commit(如果没有显式调用commit/rollback时).
     */
    @Override
    public void close() throws IOException {
        EqlUtils.closeQuietly(connection);
    }

}
