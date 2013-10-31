package org.n3r.eql;

import java.io.Closeable;
import java.sql.Connection;

public interface EqlTran extends Closeable{

    void start();

    void commit();
    
    void rollback();

    Connection getConn();
}
