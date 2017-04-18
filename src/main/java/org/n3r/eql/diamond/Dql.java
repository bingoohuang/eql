package org.n3r.eql.diamond;

import org.n3r.eql.Eql;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlDiamondConfig;

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
        return new EqlDiamondConfig(connectionName);
    }
}




