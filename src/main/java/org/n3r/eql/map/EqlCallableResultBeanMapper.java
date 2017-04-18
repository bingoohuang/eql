package org.n3r.eql.map;

import lombok.val;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.param.EqlParamPlaceholder;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class EqlCallableResultBeanMapper extends EqlBaseBeanMapper implements EqlCallableReturnMapper {
    public EqlCallableResultBeanMapper(Class<?> mappedClass) {
        super(mappedClass);
    }

    @Override
    public Object mapResult(EqlRun eqlRun, CallableStatement cs) throws SQLException {
        Object mappedObject = Reflect.on(this.mappedClass).create().get();
        val callableRs = new CallableRs(cs);

        for (int i = 0, ii = eqlRun.getPlaceHolders().length; i < ii; ++i) {
            val placeholder = eqlRun.getPlaceHolders()[i];
            if (placeholder.getInOut() != EqlParamPlaceholder.InOut.IN) {
                String field = placeholder.getPlaceholder();
                setColumnValue(callableRs, mappedObject, i + 1, field);
            }
        }

        return mappedObject;
    }

}
