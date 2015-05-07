package org.n3r.eql.trans.spring;

import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;

import java.util.HashMap;
import java.util.Map;

public class EqlTransactionManager {

    private static final String USE_LOCAL_TRANSACTION = "useThreadLocalTransaction";

    static ThreadLocal<Map<Object, Object>> eqlTranLocal = new ThreadLocal<Map<Object, Object>>() {
        @Override
        protected Map<Object, Object> initialValue() {
            Map<Object, Object> tranLocalMap = new HashMap<Object, Object>();
            tranLocalMap.put(USE_LOCAL_TRANSACTION, false);
            return tranLocalMap;
        }
    };


    public static void set(EqlConfig eqlConfig, EqlTran eqlTran) {
        Map<Object, Object> eqlTranMap = eqlTranLocal.get();
        if (eqlTranMap.get(eqlConfig) != null) return;
        eqlTranMap.put(eqlConfig, eqlTran);
    }

    public static boolean checkLocalTranEnabled() {
        return (Boolean) eqlTranLocal.get().get(USE_LOCAL_TRANSACTION);
    }

    public static EqlTran getTran(EqlConfig eqlConfig) {
        return (EqlTran) eqlTranLocal.get().get(eqlConfig);
    }

    public static void commit() {
        for (Object localValue : eqlTranLocal.get().values()) {
            if (!(localValue instanceof EqlTran)) continue;
            ((EqlTran) localValue).commit();
        }
    }

    public static void rollback() {
        for (Object localValue : eqlTranLocal.get().values()) {
            if (!(localValue instanceof EqlTran)) continue;
            ((EqlTran) localValue).rollback();
        }
    }


    public static void clear() {
        eqlTranLocal.remove();
    }

    public static void start() {
        eqlTranLocal.get().put(USE_LOCAL_TRANSACTION, true);
    }

}
