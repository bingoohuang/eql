package org.n3r.eql.map;

import org.n3r.eql.util.Rs;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EqlMapMapper implements EqlRowMapper {

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new HashMap<String, Object>();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); ++i) {
            String column = Rs.lookupColumnName(metaData, i);
            Object value = Rs.getResultSetValue(rs, i);
            row.put(column, value);
        }

        return row;
    }

}
