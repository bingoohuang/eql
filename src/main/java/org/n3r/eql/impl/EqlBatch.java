package org.n3r.eql.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.n3r.eql.Eql;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.param.EqlParamsBinder;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class EqlBatch {
    private Eql eql;
    //    private int maxBatches;
    //    private int currentBatches;
    private List<PreparedStatement> batchedPs;
    private Map<String, PreparedStatement> batchedMap;

    public EqlBatch(Eql eql) {
        this.eql = eql;
    }

    public void startBatch(int maxBatches) {
        //        this.maxBatches = maxBatches;
        startBatch();
    }

    public void startBatch() {
        batchedPs = Lists.newArrayList();
        batchedMap = Maps.newHashMap();
    }

    public int processBatchUpdate(EqlRun eqlRun) throws SQLException {
        String realSql = eqlRun.getRunSql();
        PreparedStatement ps = batchedMap.get(realSql);
        if (ps == null) {
            ps = eql.prepareSql(eqlRun);
            batchedMap.put(realSql, ps);
            batchedPs.add(ps);
        }
        new EqlParamsBinder().bindParams(ps, eqlRun, eql.getParams(), eql.getLogger());
        ps.addBatch();
        //        ++currentBatches;
        return /*maxBatches > 0 && currentBatches >= maxBatches ? executeBatch() :*/0;
    }

    public int processBatchExecution() throws SQLException {
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

            return totalRowCount;
        } finally {
            cleanupBatch();
        }
    }

    public void cleanupBatch() {
        for (PreparedStatement ps : batchedPs)
            EqlUtils.closeQuietly(ps);
    }
}
