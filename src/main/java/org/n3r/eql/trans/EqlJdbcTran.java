package org.n3r.eql.trans;

import org.n3r.eql.Eql;
import org.n3r.eql.EqlTran;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.util.Closes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class EqlJdbcTran implements EqlTran {
    private final Eql eql;
    private EqlConnection eqlConnection;
    private Connection connection;

    public EqlJdbcTran(Eql eql, EqlConnection connection) {
        this.eql = eql;
        this.eqlConnection = connection;
    }

    @Override
    public void start() {
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
        return getConnection();
    }

    protected Connection getConnection() {
        if (connection == null) connection = eqlConnection.getConnection();

        if (connection == null) throw new EqlExecuteException(
                "EqlJdbcTran could not start transaction. " +
                        " Cause: The DataSource returned a null connection.");
        try {
            if (connection.getAutoCommit()) connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new EqlExecuteException(e);
        }

        return connection;
    }

    /**
     * Oracle JDBC will auto commit when close without explicit commit/rollback.
     */
    @Override
    public void close() throws IOException {
        Closes.closeQuietly(connection);
        eql.resetTran();
    }

}
