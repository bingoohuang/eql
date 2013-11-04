package org.n3r.eql;

import com.google.common.collect.Maps;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class FreemarkerTest {
    @BeforeClass
    public static void beforeClass() {
        Eqll.choose("ftl");
        new Eqll().id("dropTestTable").execute();
        new Eqll().id("createTestTable").params(new Timestamp(1383122146000l)).execute();

        new Eqll().id("setup").execute();
    }

    @AfterClass
    public static void afterClass() {
        new Eqll().id("teardown").execute();
    }

    @Test
    public void testInner() {
        String str = new Eqll().id("testInner").limit(1).execute();
        assertThat(str, is(nullValue()));
    }

    @Test
    public void test() {
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
    }

    @Test
    public void testIn() throws Exception {
        InBean inBean = new InBean();
        new Eqll().id("testIn").params(inBean).execute();
    }

    @Test
    public void testInsertAll() throws Exception {
        InBean inBean = new InBean();
        new Eqll().id("testInsertAll").params(inBean).execute();
    }

    public static class InBean {
        private List<String> lst = new ArrayList<String>();

        {
            lst.add("1");
            lst.add("2");
            lst.add("3");
            lst.add("4");
        }

        private String[] arr;

        {
            arr = new String[]{"a", "b", "c"};
        }

        private String id = "" + System.currentTimeMillis();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getLst() {
            return lst;
        }

        public void setLst(List<String> lst) {
            this.lst = lst;
        }

        public String[] getArr() {
            return arr;
        }

        public void setArr(String[] arr) {
            this.arr = arr;
        }
    }
}
