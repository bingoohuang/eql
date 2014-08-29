package org.n3r.eql;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.util.Closes;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

//@Ignore
public class OracleSpTest {
    @BeforeClass
    public static void beforeClass() {
        Eqll.choose("orcl");
    }

    @Test
    public void procedure1() throws SQLException {
        new Eqll().update("createSpEql").execute();
        Connection connection = null;
        CallableStatement cs = null;
        try {
            connection = new Eqll().getConnection();
            cs = connection.prepareCall("{call SP_EQL(?, ?)}");
            cs.setString(1, "hjb");
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            //            System.out.println(cs.getString(2));
        } finally {
            Closes.closeQuietly(cs, connection);
        }

        String b = new Eqll()
                .procedure("callSpEql").params("hjb")
                .execute();
        assertThat(b, is("HELLO hjb"));

    }

    @Test
    public void procedure2() throws SQLException {
        new Eqll().update("createSpEql2").execute();
        List<String> bc = new Eqll()
                .procedure("callSpEql2").params("hjb")
                .execute();
        assertThat(bc.get(0), is("HELLO hjb"));
        assertThat(bc.get(1), is("WORLD hjb"));
    }

    @Test
    public void procedure3() throws SQLException {
        new Eqll().update("createSpEql2").execute();
        Map<String, String> bc = new Eqll()
                .procedure("callSpEql3").params("hjb")
                .execute();
        assertThat(bc.get("a"), is("HELLO hjb"));
        assertThat(bc.get("b"), is("WORLD hjb"));
    }

    public static class Ab {
        private String a;
        private String b;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

    }

    @Test
    public void procedure4() throws SQLException {
        new Eqll().update("createSpEql2").execute();
        Ab ab = new Eqll()
                .procedure("callSpEql4").params("hjb")
                .execute();
        assertThat(ab.getA(), is("HELLO hjb"));
        assertThat(ab.getB(), is("WORLD hjb"));
    }

    @Test
    public void procedureNoOut() throws SQLException {
        new Eqll().update("createSpNoOut").execute();
        Eql esql = new Eqll()
                .params("hjb")
                .limit(1);
        int ab = esql.returnType("int")
                .execute("{CALL SP_EQL_NOOUT(##)}", "SELECT 1 FROM DUAL");

        assertThat(ab, is(1));
    }

    @Test
    public void procedureAllOut() throws SQLException {
        new Eqll().update("createSpEql12").execute();
        List<String> rets = new Eqll().procedure("callSpEql12").execute();

        assertThat(rets.get(0), is("HELLO"));
        assertThat(rets.get(1), is("WORLD"));
    }

    @Test
    public void procedureInOut() throws SQLException {
        new Eqll().update("createSpEqlInOut").execute();
        List<String> rets = new Eqll().params("A", "B").procedure("callSpEqlInOut")
                .execute();

        assertThat(rets.get(0), is("HELLOA"));
        assertThat(rets.get(1), is("WORLDB"));
    }

    @Test
    public void createSpEqlNULL() throws SQLException {
        new Eqll().update("createSpEqlNULL").execute();
        List<String> rets = new Eqll()
                .procedure("callSpEqlNULL")
                .dynamics("SP_EQLNULL")
                .execute();

        assertThat(rets.get(0), is(nullValue()));
        assertThat(rets.get(1), is(nullValue()));
    }

    @Test
    public void returning() throws SQLException {
        new Eqll().update("prepareTable4MyProcedure").execute();
        String ret = new Eqll().procedure("myprocedure")
                .execute();

        assertThat(ret.length() > 0, is(true));
    }

    @Test
    public void returning2() throws SQLException {
        new Eqll().update("prepareTable4MyProcedure").execute();
        List<String> ret = new Eqll().params(10).procedure("myprocedure2")
                .execute();

        assertThat(ret.size() > 0, is(true));
    }

    @Test
    public void callPLSQL() throws SQLException {
        new Eqll().update("prepareTable4MyProcedure").execute();
        /*  String ret = */
        new Eqll().params(10).procedure("callPLSQL")
                .execute();
        /*  System.out.println(ret);*/
    }
}
