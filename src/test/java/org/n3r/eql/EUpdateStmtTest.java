package org.n3r.eql;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EUpdateStmtTest {
    @Before
    public void before() {
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();
    }

    @Test
    public void test() {
        Eql eql = new Eql().id("updateStmt");
        EUpdateStmt updateStmt = eql.updateStmt();

        int rows = updateStmt.update(3, "Bingoo");
        assertThat(rows, is(1));

        rows = updateStmt.update(4, "Dingoo");
        assertThat(rows, is(1));

        updateStmt.close();
        eql.close();

        String str = new Eql().selectFirst("selectStmt").params(3).execute();
        assertThat(str, is("Bingoo"));

        str = new Eql().selectFirst("selectStmt").params(4).execute();
        assertThat(str, is("Dingoo"));
    }
}
