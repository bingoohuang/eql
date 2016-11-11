package org.n3r.eql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.config.EqlConfigKeys;
import org.n3r.eql.config.EqlJdbcConfig;
import org.n3r.eql.config.EqlPropertiesConfig;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class EqlConfigTest {
    @BeforeClass
    public static void beforeClass() {
        Eqll.choose(new EqlJdbcConfig("oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@192.168.99.100:49161:xe", "system", "oracle"));
        new Eqll().execute("CREATE TABLE PERSON （id varchar2(100), sex varchar2(2), name varchar2(100))");
    }

    @AfterClass
    public static void afterClass() {
        Eqll.choose(new EqlJdbcConfig("oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@192.168.99.100:49161:xe", "system", "oracle"));
        new Eqll().execute("DROP TABLE PERSON");
    }

    @Test
    public void testEqlJdbcConfig() {
        Eqll.choose(new EqlJdbcConfig("oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@192.168.99.100:49161:xe", "system", "oracle"));

        Timestamp ts = new Eqll().limit(1).execute("SELECT SYSDATE FROM DUAL");
        assertThat(ts, not(nullValue()));
    }

    @Test
    public void testEqlPropertiesConfig() {
        Eqll.choose(new EqlPropertiesConfig(
                EqlConfigKeys.DRIVER + "=oracle.jdbc.driver.OracleDriver\n" +
                        EqlConfigKeys.URL + "=jdbc:oracle:thin:@192.168.99.100:49161:xe\n" +
                        EqlConfigKeys.USER + "=system\n" +
                        EqlConfigKeys.PASSWORD + "=oracle\n"));

        Timestamp ts = new Eqll().limit(1).execute("SELECT SYSDATE FROM DUAL");
        assertThat(ts, not(nullValue()));
    }

    public static class Person {
        private String id;
        private String sex;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }


    @Test
    public void testEqll() {
        Eqll.choose(new EqlJdbcConfig("oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@192.168.99.100:49161:xe", "system", "oracle"));
        Person pa = new Eqll().returnType(Person.class).limit(1).execute("SELECT * FROM PERSON");

        Eqll.choose(new EqlJdbcConfig("oracle.jdbc.driver.OracleDriver",
                "jdbc:oracle:thin:@192.168.99.100:49161:xe", "system", "oracle"));
        Person pb = new Eqll().returnType(Person.class).limit(1).execute("SELECT * FROM PERSON");

        Eqll.clear();
    }
}
