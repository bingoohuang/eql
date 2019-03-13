package org.n3r.eql.trans.spring;

import lombok.val;
import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.mtcp.MtcpContext;
import org.n3r.eql.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EqlTransactionManager {
    static ThreadLocal<Map<Pair<EqlConfig, String>, EqlTran>> eqlTranLocal = new ThreadLocal<>();
    static ThreadLocal<AtomicInteger> nested = new ThreadLocal<>();

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
        if (nested.get().get() > 1) return; // 忽略嵌套事务

        for (val eqlTran : eqlTranLocal.get().values()) {
            eqlTran.commit();
        }
    }

    public static void rollback() {
        if (nested.get().get() > 1) return; // 忽略嵌套事务

        for (val eqlTran : eqlTranLocal.get().values()) {
            eqlTran.rollback();
        }
    }

    public static void start() {
        AtomicInteger nests = nested.get();
        if (nests == null) {
            nested.set(new AtomicInteger(0));
        }

        nested.get().incrementAndGet();    // 记录嵌套事务

        val map = eqlTranLocal.get();
        if (map != null) return; // throw new RuntimeException("already started");

        eqlTranLocal.set(new HashMap<>());
    }

    public static void end() {
        if (nested.get().decrementAndGet() > 0) return;

        for (val eqlTran : eqlTranLocal.get().values()) {
            eqlTran.close();
        }

        eqlTranLocal.remove();
        nested.remove();
    }
}
