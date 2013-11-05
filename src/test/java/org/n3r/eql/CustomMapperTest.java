package org.n3r.eql;

import org.junit.Test;
import org.n3r.eql.map.EqlRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CustomMapperTest {
    public static class MyMapper implements EqlRowMapper {
        private String name;

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            name = rs.getString(1);
            return null;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void test() {
        MyMapper myMapper = new MyMapper();
        new Eql().returnType(myMapper).execute("SELECT 'X' FROM DUAL");
        assertThat(myMapper.getName(), is("X"));
    }
}
