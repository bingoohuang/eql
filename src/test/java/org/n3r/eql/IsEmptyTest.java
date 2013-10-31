package org.n3r.eql;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IsEmptyTest {
    @Test
    public void test1() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();

        Map<String, Object> map = Maps.newHashMap();
        map.put("a", "1");

        Eql eql = new Eql().select("isEmpty");
        List<String> strs = eql.execute();
        assertThat(strs.size(), is(2));
        assertThat(eql.getEqlRuns().get(0).getRunSql(), equalTo("SELECT B\nFROM ESQL_TEST\nWHERE A in (1,2)"));

        strs = new Eql().select("isEmpty").params(map).execute();
        assertThat(strs.size(), is(4));

        strs = new Eql().select("isNotEmpty").execute();
        assertThat(strs.size(), is(4));

        strs = new Eql().select("isNotEmpty").params(map).execute();
        assertThat(strs.size(), is(1));
    }
}
