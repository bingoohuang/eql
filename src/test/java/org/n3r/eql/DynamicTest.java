package org.n3r.eql;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.n3r.eql.map.EqlRun;

import java.sql.Timestamp;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class DynamicTest {
    @Before
    public void before() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();
    }

    @Test
    public void test1() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("a", 1);
        map.put("e", 100);
        map.put("c", "#AC");

        SimpleTest.Bean bean1 = new Eql().selectFirst("selectIf").params(map).execute();
        assertThat(bean1.toString(), is("{a:1,b:A,c:#AC,d:1383122146000,e:101}"));

        map = Maps.newHashMap();
        map.put("a", 1);
        map.put("e", 200);

        SimpleTest.Bean bean = new Eql().selectFirst("selectIf").params(map).execute();
        assertThat(bean, is(nullValue()));

        map = Maps.newHashMap();
        map.put("a", 1);
        map.put("e", 300);

        bean = new Eql().selectFirst("selectIf").params(map).execute();
        assertThat(bean, is(nullValue()));

        map = Maps.newHashMap();
        map.put("a", 1);
        map.put("e", 100);
        map.put("c", "#AC");

        SimpleTest.Bean bean2 = new Eql().selectFirst("selectIf2").params(map).execute();
        assertThat(bean2, equalTo(bean1));

        map = Maps.newHashMap();
        map.put("a", 2);
        map.put("e", 100);
        map.put("c", "#AC");

        bean = new Eql().selectFirst("selectIf2").params(map).execute();
        assertThat(bean, is(nullValue()));
    }

    @Test
    public void testUnless() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("a", 1);
        Eql eql = new Eql().params(map);
        eql.execute();
        EqlRun eqlRun = eql.getEqlRuns().get(0);
        assertThat(eqlRun.getPrintSql(), is(equalTo("SELECT A,B,C,D,E FROM EQL_TEST")));

        eql = new Eql().params("1");
        eql.execute();
        eqlRun = eql.getEqlRuns().get(0);
        assertThat(eqlRun.getPrintSql(), is(equalTo("SELECT A,B,C,D,E FROM EQL_TEST WHERE A = ?")));
    }

    @Test
    public void test2() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("a", 1);

        SimpleTest.Bean bean1 = new Eql().selectFirst("switchSelect").params(map).execute();
        assertThat(bean1.toString(), is("{a:1,b:A,c:#AC,d:1383122146000,e:101}"));
    }
}
