package org.n3r.eql;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.map.EqlRun;

import java.io.Closeable;
import java.sql.Connection;

public interface EqlTran extends Closeable, EqlTranable {
    Connection getConn(EqlConfig eqlConfig, EqlRun eqlRun);

    String getDriverName();

    String getJdbcUrl();
}
