package org.n3r.eql;

import org.junit.Test;
import org.n3r.eql.config.EqlJdbcConfig;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class EqlJdbcConfigTest {
    @Test
    public void test() {
        Eqll.choose(new EqlJdbcConfig("oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@127.0.0.1:1521:orcl", "orcl", "orcl"));

        Timestamp ts = new Eqll().limit(1).execute("SELECT SYSDATE FROM DUAL");
        assertThat(ts, not(nullValue()));
    }
}
