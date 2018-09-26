package org.n3r.eql;

import com.google.common.truth.Truth;
import lombok.Data;
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
    public void procedure2() {
        new Eqll().update("createSpEql2").execute();
        List<String> bc = new Eqll()
                .procedure("callSpEql2").params("hjb")
                .execute();
        assertThat(bc.get(0), is("HELLO hjb"));
        assertThat(bc.get(1), is("WORLD hjb"));
    }


    @Test
    public void callOutType() {
        new Eqll().update("createSpEqlType").execute();
        List<Object> bc = new Eqll()
                .procedure("callSpEqlType")
                .execute();
        Truth.assertThat(bc.get(0)).isEqualTo(Long.valueOf(18602506990L));
        Truth.assertThat(bc.get(1)).isEqualTo(Integer.valueOf(12345));
    }

    @Test
    public void procedure3() {
        new Eqll().update("createSpEql2").execute();
        Map<String, String> bc = new Eqll()
                .procedure("callSpEql3").params("hjb")
                .execute();
        assertThat(bc.get("a"), is("HELLO hjb"));
        assertThat(bc.get("b"), is("WORLD hjb"));
    }

    @Data
    public static class Ab {
        private String a;
        private String b;
    }

    @Test
    public void procedure4() {
        new Eqll().update("createSpEql2").execute();
        Ab ab = new Eqll()
                .procedure("callSpEql4").params("hjb")
                .execute();
        assertThat(ab.getA(), is("HELLO hjb"));
        assertThat(ab.getB(), is("WORLD hjb"));
    }


    @Test
    public void procedureNoOut() {
        new Eqll().update("createSpNoOut").execute();
        Eql eql = new Eqll()
                .params("hjb")
                .limit(1);
        int ab = eql.returnType("int")
                .execute("{CALL SP_EQL_NOOUT(##)}", "SELECT 1 FROM DUAL");

        assertThat(ab, is(1));
    }

    @Test
    public void procedureAllOut() {
        new Eqll().update("createSpEql12").execute();
        List<String> rets = new Eqll().procedure("callSpEql12").execute();

        assertThat(rets.get(0), is("HELLO"));
        assertThat(rets.get(1), is("WORLD"));
    }

    @Test
    public void procedureInOut() {
        new Eqll().update("createSpEqlInOut").execute();
        List<String> rets = new Eqll().params("A", "B").procedure("callSpEqlInOut")
                .execute();

        assertThat(rets.get(0), is("HELLOA"));
        assertThat(rets.get(1), is("WORLDB"));
    }

    @Test
    public void createSpEqlNULL() {
        new Eqll().update("createSpEqlNULL").execute();
        List<String> rets = new Eqll()
                .procedure("callSpEqlNULL")
                .dynamics("SP_EQLNULL")
                .execute();

        assertThat(rets.get(0), is(nullValue()));
        assertThat(rets.get(1), is(nullValue()));
    }

    @Test
    public void returning() {
        new Eqll().update("prepareTable4MyProcedure").execute();
        String ret = new Eqll().procedure("myprocedure")
                .execute();

        assertThat(ret.length() > 0, is(true));
    }

    @Test
    public void returning2() {
        new Eqll().update("prepareTable4MyProcedure").execute();
        List<String> ret = new Eqll().params(10).procedure("myprocedure2")
                .execute();

        assertThat(ret.size() > 0, is(true));
    }

    @Test
    public void callPLSQL() {
        new Eqll().update("prepareTable4MyProcedure").execute();
        /*  String ret = */
        new Eqll().params(10).procedure("callPLSQL")
                .execute();
        /*  System.out.println(ret);*/
    }

}
