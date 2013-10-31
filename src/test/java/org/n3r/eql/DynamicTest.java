package org.n3r.eql;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DynamicTest {
    @Test
    public void test1() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();

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
    public void test2() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();

        Map<String, Object> map = Maps.newHashMap();
        map.put("a", 1);

        SimpleTest.Bean bean1 = new Eql().selectFirst("switchSelect").params(map).execute();
        assertThat(bean1.toString(), is("{a:1,b:A,c:#AC,d:1383122146000,e:101}"));
    }
}
