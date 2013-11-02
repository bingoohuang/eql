package org.n3r.eql.trans;

import org.n3r.eql.config.EqlConfig;

import java.sql.Connection;

public interface EqlConnection{
    void initialize(EqlConfig eqlConfig);

    Connection getConnection();
}
