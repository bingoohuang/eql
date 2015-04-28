package org.n3r.eql.hive;

import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.map.EqlRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Ignore;

public class HiveTest {
    @Test
    @Ignore
    public void test() {
        new Eql("hive").returnType(new EqlRowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum, boolean isSingleColumn) throws SQLException {
                System.out.println("#####" + rs.getString(1));
                return null;
            }
        }).limit(3).execute("select * from TS_S_MALL_001");
    }
}
