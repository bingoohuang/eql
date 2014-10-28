package org.n3r.eql.trans;

import org.n3r.eql.Eql;
import org.n3r.eql.EqlTran;

import java.io.IOException;
import java.sql.Connection;

public class EqlJtaTran implements EqlTran {

    private final EqlConnection eqlConnection;
    private Connection connection;

    public EqlJtaTran(EqlConnection connection) {
        this.eqlConnection = connection;
    }

    @Override
    public void close() throws IOException {
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
    public Connection getConn() {
        if (connection == null) connection = eqlConnection.getConnection();

        return connection;
    }

}
