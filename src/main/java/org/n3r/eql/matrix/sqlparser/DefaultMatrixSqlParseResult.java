package org.n3r.eql.matrix.sqlparser;

import lombok.Value;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.matrix.MatrixTableFieldValue;
import org.n3r.eql.matrix.RealPartition;
import org.n3r.eql.matrix.RulesSet;

@Value
public class DefaultMatrixSqlParseResult implements MatrixSqlParseResult {
    private final RulesSet ruleSet;
    private final SqlFieldIndex[] sqlFieldIndexes;

    @Override
    public String getDatabaseName(EqlRun eqlRun) {
        MatrixTableFieldValue[] values = new MatrixTableFieldValue[sqlFieldIndexes.length];
        for (int i = 0; i < values.length; ++i) {
            values[i] = new MatrixTableFieldValue(sqlFieldIndexes[i]);
            int variantIndex = sqlFieldIndexes[i].variantIndex;
            values[i].fieldValue = "" + eqlRun.realParams.get(variantIndex)._2;
        }

        RealPartition realPartition = ruleSet.find(values);
        return realPartition.databaseName;
    }

}
