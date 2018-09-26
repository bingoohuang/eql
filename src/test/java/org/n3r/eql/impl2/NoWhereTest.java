package org.n3r.eql.impl2;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;
import org.n3r.eql.eqler.annotations.SqlOptions;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class NoWhereTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("h2").execute("DROP TABLE IF EXISTS T_NoWhereTest; CREATE TABLE T_NoWhereTest (ID INT, NAME VARCHAR(10));");
    }

    @Test
    public void noWhereUpdate() {
        String sql = "update T_NoWhereTest set name = 'bingoo'";
        try {
            new Eql("h2").execute(sql);
            fail("should not arrive here");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("where clause is required when there is no NoWhere option with the sql " + sql);
        }
    }

    @Test
    public void noWhereUpdateAllowed() {
        new Eql("h2").execute();
    }

    @Test
    public void noWhereUpdateAllowed2() {
        try {
            new Eql("h2").execute();
            fail("should not arrive here");
        } catch (Exception ex) {
            String sql = "update T_NoWhereTest set name = 'bingoo'";
            assertThat(ex.getMessage()).isEqualTo("where clause is required when there is no NoWhere option with the sql " + sql);
        }
    }

    @Test
    public void testDao() {
        NoWhereDao noWhereDao = EqlerFactory.getEqler(NoWhereDao.class);
        noWhereDao.update2();
    }

    @Test
    public void testDao2() {
        try {
            NoWhereDao noWhereDao = EqlerFactory.getEqler(NoWhereDao.class);
            noWhereDao.update1();
        } catch (Exception ex) {
            String sql = "update T_NoWhereTest set name = 'bingoo'";
            assertThat(ex.getMessage()).isEqualTo("where clause is required when there is no NoWhere option with the sql " + sql);
        }
    }

    @EqlerConfig("h2")
    public interface NoWhereDao {
        @Sql("update T_NoWhereTest set name = 'bingoo'")
        void update1();

        @Sql("update T_NoWhereTest set name = 'bingoo'")
        @SqlOptions("NoWhere")
        void update2();
    }
}
