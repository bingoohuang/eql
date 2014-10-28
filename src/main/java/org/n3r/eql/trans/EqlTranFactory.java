package org.n3r.eql.trans;

import org.n3r.eql.EqlTran;

public class EqlTranFactory {
    private final EqlConnection eqlConnection;
    private final boolean isJTA;

    public EqlTranFactory(EqlConnection eqlConnection, boolean isJTA) {
        this.eqlConnection = eqlConnection;
        this.isJTA = isJTA;
    }

    public EqlTran createTran() {
        return isJTA ? new EqlJtaTran(eqlConnection)
                : new EqlJdbcTran(eqlConnection);
    }

    public void destory() {
        try {
            eqlConnection.destroy();
        } catch (Exception e) {
            // ignore
        }
    }
}
