package org.n3r.eql;

import org.junit.Test;
import org.n3r.eql.map.EqlRun;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class SimpleInDynamicCondition {
    @Test
    public void test1() {
        List<EqlRun> eqlRuns = new Eqll().params("some").limit(1).evaluate();
        assertThat(eqlRuns.get(0).getRunSql(), is("select 'x' from ESQL_TEST\nwhere 'some' = ?"));
        eqlRuns = new Eqll().params("none").limit(1).evaluate();
        assertThat(eqlRuns.get(0).getRunSql(), is("select 'x' from ESQL_TEST"));
    }
}
