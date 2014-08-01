package org.n3r.eql.diamond;

import org.n3r.diamond.client.DiamondMiner;
import org.n3r.eql.Eql;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlPropertiesConfig;

public class Dql extends Eql {
    public Dql() {
        super(createEqlConfig(), Eql.STACKTRACE_DEEP_FIVE);
    }

    public Dql(String connectionName) {
        super(createEqlConfig(connectionName), Eql.STACKTRACE_DEEP_FIVE);
    }

    private static EqlConfig createEqlConfig() {
        return createEqlConfig(Eql.DEFAULT_CONN_NAME);
    }

    private static EqlConfig createEqlConfig(String connectionName) {
        String eqlConfig = DiamondMiner.getStone("EqlConfig", connectionName);
        return new EqlPropertiesConfig(eqlConfig);
    }
}




