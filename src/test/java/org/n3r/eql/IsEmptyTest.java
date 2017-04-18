package org.n3r.eql;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IsEmptyTest {
    @Before
    public void beforeClass() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();
    }

    @Test
    public void test1() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("a", "1");

        Eql eql = new Eql().select("isEmpty");
        List<String> strs = eql.execute();
        assertThat(strs.size(), is(2));
        assertThat(eql.getEqlRuns().get(0).getRunSql(), equalTo("SELECT B\nFROM EQL_TEST\nWHERE A in (1,2)"));

        strs = new Eql().select("isEmpty").params(map).execute();
        assertThat(strs.size(), is(10));

        eql = new Eql().select("isEmptyElse");
        eql.params(map).execute();
        assertThat(eql.getEqlRuns().get(0).getRunSql(), equalTo("SELECT B\nFROM EQL_TEST\nWHERE A = ?"));

        strs = new Eql().select("isNotEmpty").execute();
        assertThat(strs.size(), is(10));

        eql = new Eql().select("isNotEmptyElse");
        eql.execute();
        assertThat(eql.getEqlRuns().get(0).getRunSql(), equalTo("SELECT B\nFROM EQL_TEST\nWHERE A in\n(3,4)"));

        strs = new Eql().select("isNotEmpty").params(map).execute();
        assertThat(strs.size(), is(1));
    }

    @Test
    public void testInIf() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("a", "1");

        Eql eql = new Eql().select("isEmptyInIf");
        List<String> strs = eql.execute();
        assertThat(strs.size(), is(2));
        assertThat(eql.getEqlRuns().get(0).getRunSql(), equalTo("SELECT B\nFROM EQL_TEST\nWHERE A in (1,2)"));

        strs = new Eql().select("isEmptyInIf").params(map).execute();
        assertThat(strs.size(), is(10));

        eql = new Eql().select("isEmptyElseInIf");
        eql.params(map).execute();
        assertThat(eql.getEqlRuns().get(0).getRunSql(), equalTo("SELECT B\nFROM EQL_TEST\nWHERE A = ?"));

        strs = new Eql().select("isNotEmptyInIf").execute();
        assertThat(strs.size(), is(10));

        eql = new Eql().select("isNotEmptyElseInIf");
        eql.execute();
        assertThat(eql.getEqlRuns().get(0).getRunSql(), equalTo("SELECT B\nFROM EQL_TEST\nWHERE A in\n(3,4)"));

        strs = new Eql().select("isNotEmptyInIf").params(map).execute();
        assertThat(strs.size(), is(1));
    }
}
