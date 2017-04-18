package org.n3r.eql;

import org.junit.Test;
import org.n3r.eql.map.EqlRun;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class SimpleInDynamicCondition {
    @Test
    public void test1() {
        List<EqlRun> eqlRuns = new Eql().params("some").evaluate();
        EqlRun eqlRun = eqlRuns.get(0);
        assertThat(eqlRun.getRunSql(), is("select 'x' from EQL_TEST\nwhere 'some' = ?"));
        String evalSql = eqlRun.getEvalSql();
        assertThat(evalSql, is(equalTo("select 'x' from EQL_TEST where 'some' = 'some'")));

        eqlRuns = new Eql().params("none").limit(1).evaluate();
        eqlRun = eqlRuns.get(0);
        assertThat(eqlRun.getRunSql(), is("select 'x' from EQL_TEST"));
    }

    @Test
    public void test2() {
        List<EqlRun> eqlRuns = new Eql().params(123).evaluate();
        EqlRun eqlRun = eqlRuns.get(0);
        assertThat(eqlRun.getRunSql(), is("select 'x' from EQL_TEST\nwhere 'some' = ?"));
        String evalSql = eqlRun.getEvalSql();
        assertThat(evalSql, is(equalTo("select 'x' from EQL_TEST where 'some' = 123")));
    }

    @Test
    public void test3() {
        List<EqlRun> eqlRuns = new Eql().evaluate();
        EqlRun eqlRun = eqlRuns.get(0);
        assertThat(eqlRun.getRunSql(), is("select 'x' from EQL_TEST"));
        String evalSql = eqlRun.getEvalSql();
        assertThat(evalSql, is(equalTo("select 'x' from EQL_TEST")));
    }
}
