package org.n3r.eql.impl;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.param.EqlParamPlaceholder;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
public class EqlProc {
    private final EqlRun eqlRun;
    private final EqlRsRetriever rsRetriever;

    public Object dealProcedure(PreparedStatement ps) throws SQLException {
        return execAndRetrieveProcedureRet(eqlRun, (CallableStatement) ps);
    }

    private Object execAndRetrieveProcedureRet(EqlRun subSql, CallableStatement cs) throws SQLException {
        cs.execute();

        if (subSql.getOutCount() == 0) return null;

        if (subSql.getOutCount() == 1)
            for (int i = 0, ii = subSql.getPlaceHolders().length; i < ii; ++i)
                if (subSql.getPlaceHolders()[i].getInOut() != EqlParamPlaceholder.InOut.IN) return cs.getObject(i + 1);

        switch (subSql.getPlaceHolderOutType()) {
            case AUTO_SEQ:
                return retrieveAutoSeqOuts(subSql, cs);
            case VAR_NAME:
                return rsRetriever.getCallableReturnMapper().mapResult(subSql, cs);
            default:
                break;
        }

        return null;
    }

    private Object retrieveAutoSeqOuts(EqlRun subSql, CallableStatement cs) throws SQLException {
        List<Object> objects = Lists.newArrayList();
        for (int i = 0, ii = subSql.getPlaceHolders().length; i < ii; ++i)
            if (subSql.getPlaceHolders()[i].getInOut() != EqlParamPlaceholder.InOut.IN)
                objects.add(cs.getObject(i + 1));

        return objects;
    }
}
