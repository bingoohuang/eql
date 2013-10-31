package org.n3r.eql.trans;

import java.io.IOException;
import java.sql.Connection;

import org.n3r.eql.EqlTran;

public class EqlJtaTransaction implements EqlTran {

    @Override
    public void close() throws IOException {}

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
        return null;
    }

}
