package org.n3r.eql.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.experimental.var;
import lombok.val;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Closes;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.Logs;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class EqlBatch {
    private int maxBatches;
    private int currentBatches;
    private int totalBatches;
    private List<PreparedStatement> batchedPs; // for sequence use
    private Map<String, PreparedStatement> batchedMap;
    private String sqlClassPath;
    private EqlConfig eqlConfig;
    private String sqlId;
    private String tagSqlId;

    public EqlBatch() {
        this(0);
    }

    public EqlBatch(int maxBatches) {
        this.maxBatches = maxBatches;
        batchedPs = Lists.newArrayList();
        batchedMap = Maps.newHashMap();
    }

    public void prepare(String sqlClassPath, EqlConfig eqlConfig, String sqlId, String tagSqlId) {
        this.sqlClassPath = sqlClassPath;
        this.eqlConfig = eqlConfig;
        this.sqlId = sqlId;
        this.tagSqlId = tagSqlId;
    }

    public int addBatch(EqlRun eqlRun) throws SQLException {
        var ps = batchedMap.get(eqlRun.getRunSql());
        if (ps == null) {
            ps = EqlUtils.prepareSQL(sqlClassPath, eqlConfig, eqlRun, sqlId, tagSqlId);
            batchedMap.put(eqlRun.getRunSql(), ps);
            batchedPs.add(ps);
        }

        eqlRun.bindParams(ps, sqlClassPath);
        ps.addBatch();

        ++currentBatches;

        return maxBatches > 0 && currentBatches >= maxBatches
                ? executeBatch(false) : 0;
    }

    public int executeBatch() {
        return executeBatch(true);
    }

    @SneakyThrows
    public int executeBatch(boolean cleanup) {
        try {
            int totalRowCount = 0;
            for (val ps : batchedPs) {
                int[] rowCounts = ps.executeBatch();
                for (int j = 0; j < rowCounts.length; j++)
                    if (rowCounts[j] == Statement.SUCCESS_NO_INFO)
                        ; // NOTHING TO DO
                    else if (rowCounts[j] == Statement.EXECUTE_FAILED)
                        throw new EqlExecuteException(
                                "The batched statement at index " + j + " failed to execute.");
                    else totalRowCount += rowCounts[j];
            }

            totalBatches += totalRowCount;

            val eqlLog = Logs.createLogger(eqlConfig, sqlClassPath, sqlId, tagSqlId, "executeBatch");
            eqlLog.debug("current batches {} total batches {}", totalRowCount, totalBatches);
            currentBatches = 0;

            return totalBatches;
        } catch (SQLException ex) {
            cleanupBatch();
            throw ex;
        } finally {
            if (cleanup) cleanupBatch();
        }
    }

    public void cleanupBatch() {
        for (val ps : batchedPs)
            Closes.closeQuietly(ps);

        batchedPs.clear();
    }
}
