package org.n3r.eql.map;

import org.n3r.eql.joor.Reflect;
import org.n3r.eql.util.EqlUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class EqlBeanRowMapper extends EqlBaseBeanMapper implements EqlRowMapper {
    public EqlBeanRowMapper(Class<?> mappedClass) {
        super(mappedClass);
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        Object mappedObject = Reflect.on(this.mappedClass).create().get();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int index = 1; index <= columnCount; index++) {
            String column = EqlUtils.lookupColumnName(rsmd, index);
            PropertyDescriptor pd = this.mappedFields.get(column.replaceAll(" ", "").toLowerCase());
            if (pd == null) continue;

            Object value = EqlUtils.getResultSetValue(rs, index, pd.getPropertyType());
            setBeanProperty(mappedObject, pd, value);
        }

        return mappedObject;
    }

    private void setBeanProperty(Object object, PropertyDescriptor pd, Object value) {
        Method setter = pd.getWriteMethod();
        if (setter != null) {
            try {
                setter.invoke(object, value);
                return;
            } catch (Exception e) {

            }
        }

        Reflect.on(object).set(pd.getName(), value);
    }

}
