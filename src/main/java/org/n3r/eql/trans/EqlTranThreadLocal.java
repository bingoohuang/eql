package org.n3r.eql.trans;

import org.n3r.eql.EqlTran;

public class EqlTranThreadLocal {
    static ThreadLocal<EqlTran> eqlTranLocal = new ThreadLocal<>();

    public static void set(EqlTran eqlTran) {
        eqlTranLocal.set(eqlTran);
    }

    public static EqlTran get() {
        return eqlTranLocal.get();
    }

    public static void clear() {
        eqlTranLocal.remove();
    }
}
