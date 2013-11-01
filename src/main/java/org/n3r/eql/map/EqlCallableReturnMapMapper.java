package org.n3r.eql.map;

import com.google.common.collect.Maps;
import org.n3r.eql.param.EqlParamPlaceholder;
import org.n3r.eql.param.EqlParamPlaceholder.InOut;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Map;

public class EqlCallableReturnMapMapper implements EqlCallableReturnMapper {

    @Override
    public Object mapResult(EqlRun eqlRun, CallableStatement cs) throws SQLException {
        Map<String, Object> result = Maps.newHashMap();
        for (int i = 0, ii = eqlRun.getPlaceHolders().length; i < ii; ++i) {
            EqlParamPlaceholder placeholder = eqlRun.getPlaceHolders()[i];
            if (placeholder.getInOut() != InOut.IN)
                result.put(placeholder.getPlaceholder(), cs.getObject(i + 1));
        }

        return result;
    }

}
