package org.n3r.eql.trans;

import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.map.EqlRun;

import java.sql.Connection;

public class EqlJtaTran implements EqlTran {

    private final EqlConnection eqlConnection;
    private Connection connection;

    public EqlJtaTran(EqlConnection connection) {
        this.eqlConnection = connection;
    }

    @Override
    public void close() {
    }

    @Override
    public void start() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }

    @Override
    public Connection getConn(EqlConfig eqlConfig, EqlRun eqlRun) {
        if (connection == null) {
            String dbName = eqlConnection.getDbName(eqlConfig, eqlRun);
            connection = eqlConnection.getConnection(dbName);
        }

        eqlRun.setConnection(connection);
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

}
