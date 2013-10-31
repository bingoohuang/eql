package org.n3r.eql.map;

import org.n3r.eql.joor.Reflect;
import org.n3r.eql.util.EqlUtils;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class EsqlBeanRowMapper extends EsqlBaseBeanMapper implements EqlRowMapper {
    public EsqlBeanRowMapper(Class<?> mappedClass) {
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
            if (pd != null) {
                Object value = EqlUtils.getResultSetValue(rs, index, pd.getPropertyType());
                Reflect.on(mappedObject).set(pd.getName(), value);
            }
        }

        return mappedObject;
    }

}
