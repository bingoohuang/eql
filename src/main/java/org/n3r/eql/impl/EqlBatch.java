package org.n3r.eql.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.Eql;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.Closes;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class EqlBatch {
    private Eql eql;
    private int maxBatches;
    private int currentBatches;
    private int totalBatches;
    private List<PreparedStatement> batchedPs; // for sequence use
    private Map<String, PreparedStatement> batchedMap;

    public EqlBatch(Eql eql) {
        this.eql = eql;
    }

    public void startBatch(int maxBatches) {
        this.maxBatches = maxBatches;
        batchedPs = Lists.newArrayList();
        batchedMap = Maps.newHashMap();
    }

    public int addBatch(EqlRun eqlRun) throws SQLException {
        PreparedStatement ps = batchedMap.get(eqlRun.getRunSql());
        if (ps == null) {
            ps = eql.prepareSql(eqlRun);
            batchedMap.put(eqlRun.getRunSql(), ps);
            batchedPs.add(ps);
        }

        eqlRun.bindParams(ps);
        ps.addBatch();

        ++currentBatches;

        return maxBatches > 0 && currentBatches >= maxBatches
                ? executeBatch(false) : 0;
    }

    public int executeBatch() throws SQLException {
        return executeBatch(true);
    }

    public int executeBatch(boolean cleanup) throws SQLException {
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
            throw ex;
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
