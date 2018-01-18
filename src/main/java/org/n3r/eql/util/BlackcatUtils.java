package org.n3r.eql.util;

import com.github.bingoohuang.blackcat.instrument.callback.Blackcat;
import com.github.bingoohuang.blackcat.instrument.utils.Collections;
import com.github.bingoohuang.westjson.WestJson;
import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
@SuppressWarnings("unchecked")
public class BlackcatUtils {
    public boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable e) { // including ClassNotFoundException
            return false;
        }
    }

    public final boolean HasBlackcat = classExists(
            "com.github.bingoohuang.blackcat.instrument.callback.Blackcat");

    public void trace(String sqlId, String printSql,
                             String traceParams, String evalSql, Object execRet) {
        if (!HasBlackcat) return;

        String paramsAndPrepared = "[]".equals(traceParams) ? ""
                : ", Params:" + traceParams + ", Prepared:" + printSql;
        Blackcat.trace("SQL",
                "ID:" + sqlId
                        + ", SQL:" + evalSql
                        + paramsAndPrepared
                        + ", Result:" + compressResult(execRet)
        );
    }

    private Object compressResult(Object execRet) {
        if (!(execRet instanceof Collection)) {
            return new WestJson().json(execRet, WestJson.UNQUOTED);
        }

        return Collections.compressResult((Collection) execRet);
    }
}
