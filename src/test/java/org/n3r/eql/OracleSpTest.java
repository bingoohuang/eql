package org.n3r.eql;

import org.junit.Test;
import org.n3r.eql.util.EqlUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OracleSpTest {
    @Test
    public void procedure1() throws SQLException {
        new Eql().update("createSpEql").execute();
        Connection connection = null;
        CallableStatement cs = null;
        try {
            connection = new Eql().getConnection();
            cs = connection.prepareCall("{call SP_ESQL(?, ?)}");
            cs.setString(1, "hjb");
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            //            System.out.println(cs.getString(2));
        } finally {
            EqlUtils.closeQuietly(cs, connection);
        }

        String b = new Eql()
                .procedure("callSpEql").params("hjb")
                .execute();
        assertThat(b, is("HELLO hjb"));

    }

    @Test
    public void procedure2() throws SQLException {
        new Eql().update("createSpEql2").execute();
        List<String> bc = new Eql()
                .procedure("callSpEql2").params("hjb")
                .execute();
        assertEquals("HELLO hjb", bc.get(0));
        assertEquals("WORLD hjb", bc.get(1));
    }

    @Test
    public void procedure3() throws SQLException {
        new Eql().update("createSpEql2").execute();
        Map<String, Object> bc = new Eql()
                .procedure("callSpEql3").params("hjb")
                .execute();
        assertEquals("HELLO hjb", bc.get("a"));
        assertEquals("WORLD hjb", bc.get("b"));
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
        new Eql().update("createSpEql2").execute();
        Ab ab = new Eql()
                .procedure("callSpEql4").params("hjb")
                .execute();
        assertEquals("HELLO hjb", ab.getA());
        assertEquals("WORLD hjb", ab.getB());
    }

    @Test
    public void procedureNoOut() throws SQLException {
        new Eql().update("createSpNoOut").execute();
        Eql esql = new Eql()
                .params("hjb")
                .limit(1);
        int ab = esql.returnType("int")
                .execute("{CALL SP_ESQL_NOOUT(##)}", "SELECT 1 FROM DUAL");

        assertEquals(1, ab);
    }

    @Test
    public void procedureAllOut() throws SQLException {
        new Eql().update("createSpEql12").execute();
        List<String> rets = new Eql().procedure("callSpEql12").execute();

        assertEquals("HELLO", rets.get(0));
        assertEquals("WORLD", rets.get(1));
    }

    @Test
    public void procedureInOut() throws SQLException {
        new Eql().update("createSpEqlInOut").execute();
        List<String> rets = new Eql().params("A", "B").procedure("callSpEqlInOut")
                .execute();

        assertEquals("HELLOA", rets.get(0));
        assertEquals("WORLDB", rets.get(1));
    }

    @Test
    public void createSpEqlNULL() throws SQLException {
        new Eql().update("createSpEqlNULL").execute();
        List<String> rets = new Eql()
                .procedure("callSpEqlNULL")
                .dynamics("SP_ESQLNULL")
                .execute();

        assertNull(rets.get(0));
        assertNull(rets.get(1));
    }

    @Test
    public void returning() throws SQLException {
        new Eql().update("prepareTable4MyProcedure").execute();
        String ret = new Eql().procedure("myprocedure")
                .execute();

        assertTrue(ret.length() > 0);
    }

    @Test
    public void returning2() throws SQLException {
        new Eql().update("prepareTable4MyProcedure").execute();
        List<String> ret = new Eql().params(10).procedure("myprocedure2")
                .execute();

        assertTrue(ret.size() > 0);
    }

    @Test
    public void callPLSQL() throws SQLException {
        new Eql().update("prepareTable4MyProcedure").execute();
        /*  String ret = */new Eql().params(10).procedure("callPLSQL")
                .execute();
        /*  System.out.println(ret);*/
    }
}
