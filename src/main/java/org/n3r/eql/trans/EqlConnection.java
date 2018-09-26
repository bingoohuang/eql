package org.n3r.eql.trans;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.map.EqlRun;

import java.sql.Connection;

public interface EqlConnection {
    void initialize(EqlConfig eqlConfig);

    String getDbName(EqlConfig eqlConfig, EqlRun eqlRun);

    Connection getConnection(String dbName);

    void destroy();

    String getDriverName();

    String getJdbcUrl();
}
