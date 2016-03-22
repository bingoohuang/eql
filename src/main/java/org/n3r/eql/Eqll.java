package org.n3r.eql;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigCache;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.config.EqlConfigManager;
import org.n3r.eql.impl.DefaultEqlConfigDecorator;
import org.n3r.eql.util.EqlPropertiesConfigFactory;

public class Eqll extends Eql {
    static ThreadLocal<EqlConfig> eqlConfigLocal = new ThreadLocal<EqlConfig>() {
        @Override
        protected EqlConfig initialValue() {
            return EqlConfigCache.getEqlConfig(Eql.DEFAULT_CONN_NAME);
        }
    };

    public static void choose(String eqlConfigName) {
        choose(EqlPropertiesConfigFactory.parseEqlProperties(eqlConfigName));
    }

    public static void choose(EqlConfig eqlConfig) {
        clear();

        eqlConfigLocal.set(eqlConfig);
    }

    public static void clear() {
        EqlConfig oldEqlConfig = eqlConfigLocal.get();
        if (oldEqlConfig == null) return;

        EqlConfigDecorator eqlConfigDecorator;
        eqlConfigDecorator = oldEqlConfig instanceof EqlConfigDecorator
                ? (EqlConfigDecorator) oldEqlConfig
                : new DefaultEqlConfigDecorator(oldEqlConfig);
        EqlConfigManager.invalidateCache(eqlConfigDecorator);
        eqlConfigLocal.remove();

    }

    public Eqll() {
        super(eqlConfigLocal.get(), Eql.STACKTRACE_DEEP_FIVE);
    }

    public Eqll(String connectionName) {
        super(eqlConfigLocal.get(), Eql.STACKTRACE_DEEP_FIVE);
    }
}
