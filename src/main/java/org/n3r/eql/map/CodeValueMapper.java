package org.n3r.eql.map;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@AllArgsConstructor
public class CodeValueMapper implements EqlRowMapper {
    private Map<String, String> map;


    public CodeValueMapper() {
        this.map = Maps.newHashMap();
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNum, boolean isSingleColumn) throws SQLException {
        map.put(rs.getString(1), rs.getString(2));

        return null;
    }

    @EqlMappingResult
    public Map<String, String> getMap() {
        return map;
    }
}
