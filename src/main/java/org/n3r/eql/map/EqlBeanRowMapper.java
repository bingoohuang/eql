package org.n3r.eql.map;

import org.n3r.eql.joor.Reflect;
import org.n3r.eql.util.Rs;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class EqlBeanRowMapper extends EqlBaseBeanMapper implements EqlRowMapper {
    public EqlBeanRowMapper(Class<?> mappedClass) {
        super(mappedClass);
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNumber, boolean isSingleColumn) throws SQLException {
        if (isSingleColumn) {
            if (mappedClass == String.class) return rs.getString(1);
            if (mappedClass == boolean.class || mappedClass == Boolean.class) return rs.getBoolean(1);
            if (mappedClass == short.class || mappedClass == Short.class) return rs.getShort(1);
            if (mappedClass == int.class || mappedClass == Integer.class) return rs.getInt(1);
            if (mappedClass == int.class || mappedClass == Integer.class) return rs.getInt(1);
            if (mappedClass == long.class || mappedClass == Long.class) return rs.getLong(1);
            if (mappedClass == float.class || mappedClass == Float.class) return rs.getFloat(1);
            if (mappedClass == double.class || mappedClass == Double.class) return rs.getDouble(1);
            if (mappedClass == BigDecimal.class) return rs.getBigDecimal(1);

            Object object = rs.getObject(1);
            if (object != null && mappedClass.isAssignableFrom(object.getClass())) return object;
        }

        Object mappedObject = Reflect.on(this.mappedClass).create().get();

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        RsAware rsAware = new ResultSetRs(rs);

        for (int index = 1; index <= columnCount; index++) {
            String column = Rs.lookupColumnName(rsmd, index);
            boolean succ = setColumnValue(rsAware, mappedObject, index, column);
            if (!succ && isSingleColumn) return rs.getObject(1);

        }

        return mappedObject;
    }

}
