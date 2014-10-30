package org.n3r.eql.impl;

import com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.map.EqlRun;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class InSqlTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").execute();
    }

    @Test
    public void test() {
        ArrayList<String> ids = Lists.newArrayList("1", "2");
        Eql eql = new Eql("mysql").params(ids);
        List<String> result = eql.execute();
        assertThat(result, is(equalTo((List<String>) of("AA", "BB"))));
    }

    @Test
    public void testDynamic() {
        ArrayList<String> ids = Lists.newArrayList("1", "2");
        Eql eql = new Eql("mysql").params(ids).dynamics("EQL_IN");
        List<String> result = eql.execute();
        assertThat(result, is(equalTo((List<String>) of("AA", "BB"))));
        EqlRun eqlRun = eql.getEqlRuns().get(0);
        assertThat(eqlRun.getEvalSql(), is(equalTo("SELECT NAME FROM EQL_IN WHERE ID IN ( '1','2' )")));
    }
}
