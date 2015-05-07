package org.n3r.eql.trans.spring;

import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;

import java.util.HashMap;
import java.util.Map;

public class EqlTransactionManager {
    static ThreadLocal<Map<EqlConfig, EqlTran>> eqlTranLocal = new ThreadLocal<Map<EqlConfig, EqlTran>>();

    public static EqlTran getTran(EqlConfig eqlConfig) {
        return eqlTranLocal.get().get(eqlConfig);
    }

    public static void setTran(EqlConfig eqlConfig, EqlTran eqlTran) {
        Map<EqlConfig, EqlTran> eqlTranMap = eqlTranLocal.get();
        if (eqlTranMap == null) throw new RuntimeException("transaction not started");

        EqlTran oldEqlTran = eqlTranMap.get(eqlConfig);
        if (oldEqlTran != null) throw new RuntimeException("transaction already exists");

        eqlTranMap.put(eqlConfig, eqlTran);
    }

    public static boolean isEqlTransactionEnabled() {
        return eqlTranLocal.get() != null;
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

    public static void start() {
        Map<EqlConfig, EqlTran> map = eqlTranLocal.get();
        if (map != null) throw new RuntimeException("already started");

        eqlTranLocal.set(new HashMap<EqlConfig, EqlTran>());
    }

    public static void clear() {
        eqlTranLocal.remove();
    }


}
