package org.n3r.eql.trans;

import org.n3r.eql.Eql;
import org.n3r.eql.EqlTran;

public class EqlTranFactory {
    private final EqlConnection eqlConnection;
    private final boolean isJTA;

    public EqlTranFactory(EqlConnection eqlConnection, boolean isJTA) {
        this.eqlConnection = eqlConnection;
        this.isJTA = isJTA;
    }

    public EqlTran createTran(Eql eql) {
        // Connection connection = eqlConnection.getConnection();

        return isJTA ? new EqlJtaTran(eql, eqlConnection)
                : new EqlJdbcTran(eql, eqlConnection);
    }

    public void destory() {
        try {
            eqlConnection.destroy();
        } catch (Exception e) {
            // ignore
        }
    }
}
