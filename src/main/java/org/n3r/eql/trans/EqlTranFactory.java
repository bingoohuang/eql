package org.n3r.eql.trans;

import org.n3r.eql.EqlTran;

import java.sql.Connection;

public class EqlTranFactory {
    private final EqlConnection eqlConnection;
    private final boolean isJTA;

    public EqlTranFactory(EqlConnection eqlConnection, boolean isJTA) {
        this.eqlConnection = eqlConnection;
        this.isJTA = isJTA;
    }

    public EqlTran createTran() {
        Connection connection = eqlConnection.getConnection();

        return isJTA ? new EqlJtaTran(connection)
                : new EqlJdbcTran(connection);
    }
}
