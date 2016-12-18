package org.n3r.eql.util;

import org.n3r.eql.config.EqlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Logs {
    public static void logResult(EqlConfig eqlConfig, String sqlClassPath, Object execRet, String sqlId, String tagSqlId) {
        Logger logger = createLogger(eqlConfig, sqlClassPath, sqlId, tagSqlId, "result");
//        if (!logger.isDebugEnabled()) return;

        if (!(execRet instanceof List)) {
            logger.debug("" + execRet);
                BlackcatUtils.log("SQL.RESULT", "" + execRet);
            return;
        }

        List list = (List) execRet;
        int size = list.size();
        int logMaxRows = EqlUtils.getConfigInt(eqlConfig, "result.log.max", 50);

        if (size > logMaxRows) {
            List logRows = list.subList(0, logMaxRows);
            logger.debug("first {}/{} rows: {}", logMaxRows, size, logRows);
            BlackcatUtils.log("SQL.RESULT", "first {}/{} rows: {}", logMaxRows, size, logRows);
        } else {
            logger.debug("total {} rows of: {}", size, list);
            BlackcatUtils.log("SQL.RESULT", "total {} rows of: {}", size, list);
        }
    }

    public static Logger createLogger(EqlConfig eqlConfig, String sqlClassPath, String sqlId,  String tagSqlId, String tag) {
        String loggerPrefix = eqlConfig.getStr("logger.prefix");
        if (S.isBlank(loggerPrefix) || loggerPrefix.equals("auto")) loggerPrefix = "eql";
        if (loggerPrefix.endsWith(".")) loggerPrefix = loggerPrefix.substring(0, loggerPrefix.length() - 1);

        String sqlClassPathNullable = sqlClassPath == null ? "null" : sqlClassPath;
        String thisSqlId = S.isNotBlank(tagSqlId) ? tagSqlId : sqlId;
        String loggerName = loggerPrefix + '.' + sqlClassPathNullable.replace('/', '.') + '.' + thisSqlId + '.' + tag;
        String stripDollarLoggerName = loggerName.replace('$', '_');
        return LoggerFactory.getLogger(stripDollarLoggerName);
    }
}
