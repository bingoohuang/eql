package org.n3r.eql;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigCache;
import org.n3r.eql.util.EqlPropertiesConfigFactory;

public class Eqll extends Eql {
    private static ThreadLocal<EqlConfig> eqlConfigLocal = new ThreadLocal<EqlConfig>(){
        @Override
        protected EqlConfig initialValue() {
            return EqlConfigCache.getEqlConfig(Eql.DEFAULT_CONN_NAME);
        }
    };

    public static void choose(String eqlConfigName) {
        choose(EqlPropertiesConfigFactory.parseEqlProperties(eqlConfigName));
    }
    public static void choose(EqlConfig eqlConfigable) {
        eqlConfigLocal.set(eqlConfigable);
    }

    public Eqll() {
        super(eqlConfigLocal.get(), 5);
    }
}
