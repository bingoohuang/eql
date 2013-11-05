package org.n3r.eql;

import org.junit.Test;
import org.n3r.eql.config.EqlConfigKeys;
import org.n3r.eql.config.EqlJdbcConfig;
import org.n3r.eql.config.EqlPropertiesConfig;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class EqlConfigTest {
    @Test
    public void testEqlJdbcConfig() {
        Eqll.choose(new EqlJdbcConfig("oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@127.0.0.1:1521:orcl", "orcl", "orcl"));

        Timestamp ts = new Eqll().limit(1).execute("SELECT SYSDATE FROM DUAL");
        assertThat(ts, not(nullValue()));
    }

    @Test
    public void testEqlPropertiesConfig() {
        Eqll.choose(new EqlPropertiesConfig(
                EqlConfigKeys.DRIVER + "=oracle.jdbc.driver.OracleDriver\n" +
                EqlConfigKeys.URL + "=jdbc:oracle:thin:@127.0.0.1:1521:orcl\n" +
                EqlConfigKeys.USER + "=orcl\n" +
                EqlConfigKeys.PASSWORD + "=orcl\n"));

        Timestamp ts = new Eqll().limit(1).execute("SELECT SYSDATE FROM DUAL");
        assertThat(ts, not(nullValue()));
    }
}
