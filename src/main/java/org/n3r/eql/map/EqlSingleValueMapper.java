package org.n3r.eql.map;

import org.n3r.eql.util.Rs;

import java.sql.ResultSet;

public class EqlSingleValueMapper implements EqlRowMapper {

    @Override
    public Object mapRow(ResultSet rs, int rowNum, boolean isSingleColumn) {
        return Rs.getResultSetValue(rs, 1);
    }

}
