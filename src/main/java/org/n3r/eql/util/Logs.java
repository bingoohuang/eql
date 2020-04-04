package org.n3r.eql.util;

import com.github.bingoohuang.FlipTable;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.n3r.eql.config.EqlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Logs {
    public static final boolean HAS_FLIP_TABLE_CONVERTERS
            = BlackcatUtils.classExists("com.github.bingoohuang.FlipTable");


    public static void logResult(EqlConfig eqlConfig, String sqlClassPath, Object execRet, String sqlId, String tagSqlId) {
        Logger logger = createLogger(eqlConfig, sqlClassPath, sqlId, tagSqlId, "result");

        if (!(execRet instanceof List)) {
            logger.debug("" + execRet);
            return;
        }

        List list = (List) execRet;
        int size = list.size();
        if (size == 0) {
            logger.debug("total 0 rows");
            return;
        }

        int logMaxRows = EqlUtils.getConfigInt(eqlConfig, "result.log.max", 20);

        if (size > logMaxRows) {
            List logRows = list.subList(0, logMaxRows);
            if (HAS_FLIP_TABLE_CONVERTERS) {
                String table = FlipTable.of(logRows);
                logger.debug("first {}/{} rows: \n{}", logMaxRows, size, table);
            } else {
                logger.debug("first {}/{} rows: {}", logMaxRows, size, logRows);
            }
        } else {
            if (HAS_FLIP_TABLE_CONVERTERS) {
                String table = FlipTable.of(list);
                logger.debug("total {} rows: \n{}", size, table);
            } else {
                logger.debug("total {} rows: {}", size, list);
            }
        }
    }

    public static Logger createLogger(EqlConfig eqlConfig, String sqlClassPath, String sqlId, String tagSqlId, String tag) {
        String loggerPrefix = eqlConfig.getStr("logger.prefix");
        if (S.isBlank(loggerPrefix) || loggerPrefix.equals("auto")) loggerPrefix = "eql";
        if (loggerPrefix.endsWith(".")) loggerPrefix = loggerPrefix.substring(0, loggerPrefix.length() - 1);

        val sqlClassPathNullable = StringUtils.defaultString(sqlClassPath, "null");
        val thisSqlId = S.isNotBlank(tagSqlId) ? tagSqlId : sqlId;
        val loggerName = loggerPrefix + '.' + sqlClassPathNullable.replace('/', '.') + '.' + thisSqlId + '.' + tag;
        val stripDollarLoggerName = loggerName.replace('$', '_');
        return LoggerFactory.getLogger(stripDollarLoggerName);
    }
}
