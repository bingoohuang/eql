package org.n3r.eql.trans;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Closes;

import java.sql.Connection;
import java.util.Map;

public class EqlJdbcTran implements EqlTran {
    private EqlConnection eqlConnection;
    private Map<String, Connection> connections = Maps.newHashMap();

    public EqlJdbcTran(EqlConnection connection) {
        this.eqlConnection = connection;
    }

    @Override
    public void start() {
    }

    @Override @SneakyThrows
    public void commit() {
        for (Connection connection : connections.values()) {
            connection.commit();
        }
    }

    @Override @SneakyThrows
    public void rollback() {
        for (Connection connection : connections.values()) {
            connection.rollback();
        }
    }

    @Override @SneakyThrows
    public Connection getConn(EqlConfig eqlConfig, EqlRun eqlRun) {
        String dbName = eqlConnection.getDbName(eqlConfig, eqlRun);
        Connection connection = connections.get(dbName);
        if (connection != null) {
            eqlRun.setConnection(connection);
            return connection;
        }

        connection = eqlConnection.getConnection(dbName);

        if (connection == null) throw new EqlExecuteException(
                "EqlJdbcTran could not start transaction. " +
                        " Cause: The DataSource returned a null connection.");
        if (connection.getAutoCommit()) connection.setAutoCommit(false);

        connections.put(dbName, connection);
        if (eqlRun != null) eqlRun.setConnection(connection);

        return connection;
    }

    @Override
    public String getDriverName() {
        return eqlConnection.getDriverName();
    }

    @Override
    public String getJdbcUrl() {
        return eqlConnection.getJdbcUrl();
    }

    /**
     * Oracle JDBC will auto commit when close without explicit commit/rollback.
     */
    @Override
    public void close() {
        for (Connection connection : connections.values()) {
            Closes.closeQuietly(connection);
        }
    }

}
