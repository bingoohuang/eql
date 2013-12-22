package org.n3r.eql.matrix.sqlparser;

import org.n3r.eql.map.EqlRun;
import org.n3r.eql.matrix.MatrixTableFieldValue;
import org.n3r.eql.matrix.RealPartition;
import org.n3r.eql.matrix.RulesSet;

public class DefaultMatrixSqlParseResult implements MatrixSqlParseResult {
    private final SqlFieldIndex[] sqlFieldIndexes;
    private final RulesSet ruleSet;

    public DefaultMatrixSqlParseResult(RulesSet ruleSet, SqlFieldIndex[] sqlFieldIndexes) {
        this.ruleSet = ruleSet;
        this.sqlFieldIndexes = sqlFieldIndexes;
    }

    @Override
    public String getDatabaseName(EqlRun eqlRun) {
        MatrixTableFieldValue[] values = new MatrixTableFieldValue[sqlFieldIndexes.length];
        for (int i = 0; i < values.length; ++i) {
            values[i] = new MatrixTableFieldValue(sqlFieldIndexes[i]);
            values[i].fieldValue = "" + eqlRun.realParams.get(sqlFieldIndexes[i].variantIndex).getRight();
        }

        RealPartition realPartition = ruleSet.find(values);
        return realPartition.databaseName;
    }

}
