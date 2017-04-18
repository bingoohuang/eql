package org.n3r.eql.config;

import com.google.common.cache.*;
import lombok.val;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.trans.EqlConnection;
import org.n3r.eql.trans.EqlJndiConnection;
import org.n3r.eql.trans.EqlSimpleConnection;
import org.n3r.eql.trans.EqlTranFactory;
import org.n3r.eql.util.S;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EqlConfigManager {
    private static LoadingCache<EqlConfigDecorator, EqlTranFactory>
            eqlTranFactoryCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<EqlConfigDecorator, EqlTranFactory>() {
                @Override
                public void onRemoval(RemovalNotification<EqlConfigDecorator, EqlTranFactory> notification) {
                    notification.getKey().onRemoval();

                    try {
                        notification.getValue().destory();
                    } catch (Exception e) {
                        // ignore exception
                    }
                }
            })
            .build(new CacheLoader<EqlConfigDecorator, EqlTranFactory>() {
                @Override
                public EqlTranFactory load(EqlConfigDecorator eqlConfig) throws Exception {
                    eqlConfig.onLoad();

                    return createEqlTranFactory(eqlConfig);
                }
            });

    private static EqlTranFactory createEqlTranFactory(EqlConfig eqlConfig) {
        val eqlConn = createEqlConnection(eqlConfig, EqlConfigKeys.CONNECTION_IMPL);
        eqlConn.initialize(eqlConfig);

        String tranType = eqlConfig.getStr(EqlConfigKeys.TRANSACTION_TYPE);
        return new EqlTranFactory(eqlConn, EqlConfigKeys.JTA.equalsIgnoreCase(tranType));
    }

    public static EqlConnection createEqlConnection(EqlConfig eqlConfig, String implKey) {
        String eqlConfigClass = eqlConfig.getStr(implKey);

        if (S.isBlank(eqlConfigClass)) {
            String jndiName = eqlConfig.getStr(EqlConfigKeys.JNDI_NAME);
            return S.isBlank(jndiName)
                    ? new EqlSimpleConnection()
                    : new EqlJndiConnection();
        }

        return Reflect.on(eqlConfigClass).create().get();
    }

    public static EqlTranFactory getConfig(EqlConfigDecorator eqlConfig) {
        try {
            return eqlTranFactoryCache.get(eqlConfig);
        } catch (ExecutionException e) {
            throw new EqlConfigException("EqlConfig " + eqlConfig
                    + " is not properly configured.", e.getCause());
        }
    }

    public static void invalidateCache(EqlConfigDecorator eqlConfig) {
        eqlTranFactoryCache.invalidate(eqlConfig);
    }
}
