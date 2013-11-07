package org.n3r.eql.diamond;

import org.n3r.diamond.client.DiamondMiner;
import org.n3r.eql.Eql;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlPropertiesConfig;

public class Dql extends Eql {
    public Dql() {
        this(Eql.DEFAULT_CONN_NAME);
    }

    public Dql(String connectionName) {
        super(createEqlConfig(connectionName));
    }

    private static EqlConfig createEqlConfig(String connectionName) {
        String eqlConfig = DiamondMiner.getStone("EqlConfig", connectionName);
        return new EqlPropertiesConfig(eqlConfig);
    }
}




