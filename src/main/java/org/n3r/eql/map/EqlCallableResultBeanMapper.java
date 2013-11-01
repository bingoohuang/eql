package org.n3r.eql.map;

import org.n3r.eql.joor.Reflect;
import org.n3r.eql.param.EqlParamPlaceholder;

import java.beans.PropertyDescriptor;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class EqlCallableResultBeanMapper extends EqlBaseBeanMapper implements EqlCallableReturnMapper {

    public EqlCallableResultBeanMapper(Class<?> mappedClass) {
        super(mappedClass);
    }

    @Override
    public Object mapResult(EqlRun eqlRun, CallableStatement cs) throws SQLException {
        Object mappedObject = Reflect.on(this.mappedClass).create().get();

        for (int i = 0, ii = eqlRun.getPlaceHolders().length; i < ii; ++i) {
            EqlParamPlaceholder placeholder = eqlRun.getPlaceHolders()[i];
            if (placeholder.getInOut() != EqlParamPlaceholder.InOut.IN) {
                String field = placeholder.getPlaceholder();
                PropertyDescriptor pd = this.mappedFields.get(field.toLowerCase());
                if (pd != null) {
                    Object object = cs.getObject(i + 1);
                    Reflect.on(mappedObject).set(pd.getName(), object);
                }
            }
        }

        return mappedObject;
    }

}
