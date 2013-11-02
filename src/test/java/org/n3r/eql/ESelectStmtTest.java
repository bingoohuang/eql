package org.n3r.eql;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ESelectStmtTest {
    @Test
    public void test() {
        Eql eql = new Eql().id("selectStmt");
        ESelectStmt selectStmt = eql.selectStmt();

        selectStmt.executeQuery(3);
        String str = selectStmt.next();
        assertThat(str, is("CC"));
        assertThat(selectStmt.next(), is(nullValue()));

        selectStmt.executeQuery(4);
        str = selectStmt.next();
        assertThat(str, is("DC"));
        assertThat(selectStmt.next(), is(nullValue()));

        selectStmt.close();
        eql.close();
    }
}
