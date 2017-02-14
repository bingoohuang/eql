package org.n3r.eql.util;

import com.github.bingoohuang.blackcat.instrument.callback.Blackcat;
import com.google.common.collect.Iterables;
import lombok.val;

import java.util.Collection;

public class BlackcatUtils {
    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable e) { // including ClassNotFoundException
            return false;
        }
    }

    public static final boolean HasBlackcat = classExists(
            "com.github.bingoohuang.blackcat.instrument.callback.Blackcat");

    public static void trace(String printSql, String traceParams, String evalSql, Object execRet) {
        if (!HasBlackcat) return;

        Blackcat.trace("SQL", evalSql
                + "##" + compressResult(execRet)
                + ("[]".equals(traceParams) ? "" : "##" + traceParams + "##" + printSql));
    }

    private static Object compressResult(Object execRet) {
        if (!(execRet instanceof Collection)) return execRet;

        val col = (Collection) execRet;
        int size = col.size();
        if (size > 10) {
            return "10/" + size + ":" + Iterables.limit(col, 10);
        } else {
            return size + "/" + size + ":" + execRet;
        }
    }
}
