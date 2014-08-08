package org.n3r.eql.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.trans.EqlConnection;
import org.n3r.eql.trans.EqlJndiConnection;
import org.n3r.eql.trans.EqlSimpleConnection;
import org.n3r.eql.trans.EqlTranFactory;
import org.n3r.eql.util.EqlUtils;

import java.util.concurrent.ExecutionException;

public class EqlConfigManager {
    private static LoadingCache<EqlConfig, EqlTranFactory> eqlConfigableCache =
            CacheBuilder.newBuilder().build(
                    new CacheLoader<EqlConfig, EqlTranFactory>() {
                        @Override
                        public EqlTranFactory load(EqlConfig eqlConfig) throws Exception {
                            return createEqlTranFactory(eqlConfig);
                        }
                    }
            );

    private static EqlTranFactory createEqlTranFactory(EqlConfig eqlConfig) {
        EqlConnection eqlConnection = createEqlConnection(eqlConfig, EqlConfigKeys.CONNECTION_IMPL);
        eqlConnection.initialize(eqlConfig);

        return new EqlTranFactory(eqlConnection,
                EqlConfigKeys.JTA.equalsIgnoreCase(eqlConfig.getStr(EqlConfigKeys.TRANSACTION_TYPE)));
    }

    public static EqlConnection createEqlConnection(EqlConfig eqlConfig, String implKey) {
        String eqlConfigClass = eqlConfig.getStr(implKey);

        if (EqlUtils.isBlank(eqlConfigClass)) {
            String jndiName = eqlConfig.getStr(EqlConfigKeys.JNDI_NAME);
            return EqlUtils.isBlank(jndiName) ?
                    new EqlSimpleConnection() : new EqlJndiConnection();
        }

        return Reflect.on(eqlConfigClass).create().get();
    }

    public static EqlTranFactory getConfig(EqlConfig eqlConfig) {
        try {
            return eqlConfigableCache.get(eqlConfig);
        } catch (ExecutionException e) {
            throw new EqlConfigException("EqlConfig " + eqlConfig
                            + " is not properly configed.", e.getCause());
        }
    }
}
