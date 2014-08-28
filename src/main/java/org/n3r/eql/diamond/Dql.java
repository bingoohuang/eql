package org.n3r.eql.diamond;

import org.n3r.diamond.client.Miner;
import org.n3r.eql.Eql;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlPropertiesConfig;

import java.util.Properties;

public class Dql extends Eql {
    public Dql() {
        super(createEqlConfig(), Eql.STACKTRACE_DEEP_FIVE);
    }

    public Dql(String connectionName) {
        super(createEqlConfig(connectionName), Eql.STACKTRACE_DEEP_FIVE);
    }

    public static EqlConfig createEqlConfig() {
        return createEqlConfig(Eql.DEFAULT_CONN_NAME);
    }

    public static EqlConfig createEqlConfig(String connectionName) {
        Properties eqlConfig = new Miner().getProperties("EqlConfig", connectionName);
        return new EqlPropertiesConfig(eqlConfig);
    }
}




