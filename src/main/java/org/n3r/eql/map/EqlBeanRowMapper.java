package org.n3r.eql.map;

import org.n3r.eql.joor.Reflect;
import org.n3r.eql.util.Rs;

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
        RsAware rsAware = new ResultSetRs(rs);

        for (int index = 1; index <= columnCount; index++) {
            String column = Rs.lookupColumnName(rsmd, index);
            setColumnValue(rsAware, mappedObject, index, column);
        }

        return mappedObject;
    }

}
