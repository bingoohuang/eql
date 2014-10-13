package org.n3r.eql.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Logs {
    protected static Logger logger = LoggerFactory.getLogger(Logs.class);

    public static void logResult(Object execRet, String sqlId) {
        if (!logger.isDebugEnabled()) return;

        if (!(execRet instanceof List)) {
            logger.debug("result for [{}]: {}", sqlId, execRet);
            return;
        }

        List list = (List) execRet;
        int size = list.size();
        int logMaxRows = 50;
        if (size > logMaxRows) {
            List logRows = list.subList(0, logMaxRows);
            logger.debug("first {}/{} rows of result for [{}]: {}", logMaxRows, size, sqlId, logRows);
        } else {
            logger.debug("total {} rows of result for [{}]: {}", size, sqlId, list);
        }
    }

}
