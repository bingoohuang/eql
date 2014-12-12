package org.n3r.eql.impl;

import com.google.common.collect.Maps;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.util.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DecodeMapper implements EqlRowMapper {
    final Map<String, Pair<String, String>> def = Maps.newHashMap();

    Map<String, String> map = Maps.newHashMap();

    public DecodeMapper(String... defs) {
        for (int i = 0; i + 3 <= defs.length; i += 3) {
            def.put(defs[i], Pair.of(defs[i + 1], defs[i + 2]));
        }
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        String code = rs.getString(1);
        String value = rs.getString(2);

        Pair<String, String> pair = def.get(code);
        map.put(pair._1, code);
        map.put(pair._2, value);

        return null;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
