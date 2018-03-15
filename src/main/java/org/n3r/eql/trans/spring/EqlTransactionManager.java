package org.n3r.eql.trans.spring;

import lombok.val;
import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.mtcp.MtcpContext;
import org.n3r.eql.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class EqlTransactionManager {
    static ThreadLocal<Map<Pair<EqlConfig, String>, EqlTran>> eqlTranLocal;

    static {
        eqlTranLocal = new ThreadLocal<Map<Pair<EqlConfig, String>, EqlTran>>();
    }

    public static EqlTran getTran(EqlConfig eqlConfig) {
        val pair = Pair.of(eqlConfig, MtcpContext.getTenantId());
        return eqlTranLocal.get().get(pair);
    }

    public static void setTran(EqlConfig eqlConfig, EqlTran eqlTran) {
        val eqlTranMap = eqlTranLocal.get();
        if (eqlTranMap == null)
            throw new RuntimeException("transaction not started");

        val pair = Pair.of(eqlConfig, MtcpContext.getTenantId());
        val oldEqlTran = eqlTranMap.get(pair);
        if (oldEqlTran != null)
            throw new RuntimeException("transaction already exists");

        eqlTranMap.put(pair, eqlTran);
    }

    public static boolean isEqlTransactionEnabled() {
        return eqlTranLocal.get() != null;
    }

    public static void commit() {
        for (val eqlTran : eqlTranLocal.get().values()) {
            eqlTran.commit();
        }
    }

    public static void rollback() {
        for (val eqlTran : eqlTranLocal.get().values()) {
            eqlTran.rollback();
        }
    }

    public static void start() {
        val map = eqlTranLocal.get();
        if (map != null) throw new RuntimeException("already started");

        eqlTranLocal.set(new HashMap<Pair<EqlConfig, String>, EqlTran>());
    }

    public static void end() {
        for (val eqlTran : eqlTranLocal.get().values()) {
            eqlTran.close();
        }

        eqlTranLocal.remove();
    }
}
