package org.n3r.eql.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.n3r.eql.ex.EqlConfigException;

import java.util.concurrent.ExecutionException;

public class EqlConfigManager {
    private static LoadingCache<Object, EqlTranAware> esqlConfigableCache =
            CacheBuilder.newBuilder().build(
                    new CacheLoader<Object, EqlTranAware>() {
                        @Override
                        public EqlTranAware load(Object connNameOrConfigable) throws Exception {
                            Configable config = null;
                            if (connNameOrConfigable instanceof String) {
                                config = EqlConfig.parseConfig("" + connNameOrConfigable);
                            } else if (connNameOrConfigable instanceof Configable) {
                                config = (Configable) connNameOrConfigable;
                            }

                            if (config == null || config.getProperties().size() == 0) return null;

                            return config.exists("jndiName")
                                    ? createDsConfig(config) : createSimpleConfig(config);
                        }
                    }
            );

    private static EqlTranAware createDsConfig(Configable connConfig) {
        EqlDsConfig dsConfig = new EqlDsConfig();
        dsConfig.setJndiName(connConfig.getStr("jndiName"));
        dsConfig.setInitial(connConfig.getStr("java.naming.factory.initial", ""));
        dsConfig.setUrl(connConfig.getStr("java.naming.provider.url", ""));
        dsConfig.setTransactionType(connConfig.getStr("transactionType"));

        return dsConfig;
    }

    private static EqlTranAware createSimpleConfig(Configable connConfig) {
        EqlSimpleConfig simpleConfig = new EqlSimpleConfig();
        simpleConfig.setDriver(connConfig.getStr("driver"));
        simpleConfig.setUrl(connConfig.getStr("url"));
        simpleConfig.setUser(connConfig.getStr("user"));
        simpleConfig.setPass(connConfig.getStr("password"));
        simpleConfig.setTransactionType(connConfig.getStr("transactionType"));

        return simpleConfig;
    }

    public static EqlTranAware getConfig(Object connectionNameOrConfigable) {
        try {
            return esqlConfigableCache.get(connectionNameOrConfigable);
        } catch (ExecutionException e) {
            throw new EqlConfigException(
                    "eql connection name " + connectionNameOrConfigable
                            + " is not properly configed.", e.getCause());
        }


    }
}
