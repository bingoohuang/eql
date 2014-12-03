package org.n3r.eql.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigDecorator;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Closes;
import org.n3r.eql.util.EqlUtils;

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

    public EqlBatch() {
        this(0);
    }

    public EqlBatch(int maxBatches) {
        this.maxBatches = maxBatches;
        batchedPs = Lists.newArrayList();
        batchedMap = Maps.newHashMap();
    }

    public int addBatch(EqlConfig eqlConfig, EqlRun eqlRun, String sqlId) throws SQLException {
        PreparedStatement ps = batchedMap.get(eqlRun.getRunSql());
        if (ps == null) {
            ps = EqlUtils.prepareSql(eqlConfig, eqlRun, sqlId);
            batchedMap.put(eqlRun.getRunSql(), ps);
            batchedPs.add(ps);
        }

        eqlRun.bindParams(ps);
        ps.addBatch();

        ++currentBatches;

        return maxBatches > 0 && currentBatches >= maxBatches
                ? executeBatch(false) : 0;
    }

    public int executeBatch()  {
        return executeBatch(true);
    }

    public int executeBatch(boolean cleanup)  {
        try {
            int totalRowCount = 0;
            for (PreparedStatement ps : batchedPs) {
                int[] rowCounts = ps.executeBatch();
                for (int j = 0; j < rowCounts.length; j++)
                    if (rowCounts[j] == Statement.SUCCESS_NO_INFO) ; // NOTHING TO DO
                    else if (rowCounts[j] == Statement.EXECUTE_FAILED) throw new EqlExecuteException(
                            "The batched statement at index " + j + " failed to execute.");
                    else totalRowCount += rowCounts[j];
            }

            totalBatches += totalRowCount;
            currentBatches = 0;

            return totalBatches;
        } catch (SQLException ex) {
            cleanupBatch();
            throw new EqlExecuteException(ex);
        } finally {
            if (cleanup) cleanupBatch();
        }
    }

    public void cleanupBatch() {
        for (PreparedStatement ps : batchedPs)
            Closes.closeQuietly(ps);

        batchedPs.clear();
    }
}
