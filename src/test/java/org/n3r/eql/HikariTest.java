package org.n3r.eql;

import org.junit.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class HikariTest {
    @Test
    public void test1() throws IOException {
        String str = new Eql("hikari").selectFirst("test1").execute();
        assertThat(str, is("1"));

        Integer i = new Eql("hikari").selectFirst("getInt").execute();
        assertThat(i, is(1));

        str = new Eql("hikari").selectFirst("getStringWithOneParam").params("x").execute();
        assertThat(str, is("x"));

        str = new Eql("hikari").selectFirst("getStringWithOneParam").params("Y").execute();
        assertThat(str, is(nullValue()));


        str = new Eql("hikari").selectFirst("getStringWithTwoParams").params("x", "y").execute();
        assertThat(str, is("x"));

        str = new Eql("hikari").selectFirst("getStringWithTwoParamsAndSequence").params("y", "x").execute();
        assertThat(str, is("x"));
    }


    @Test
    public void test2() {
        Map<String, String> ret = new Eql("hikari").selectFirst("queryXY").execute();
        assertThat(ret.get("X"), is("x"));
        assertThat(ret.get("Y"), is("y"));

        List<Map<String, String>> lst = new Eql("hikari").select("queryXY2").execute();
        assertThat(lst.size(), is(2));
        assertThat(lst.get(0), equalTo(map("X", "x0", "Y", "y0")));
        assertThat(lst.get(1), equalTo(map("X", "x1", "Y", "y1")));
    }

    @Test
    public void test3() {
        new Eql("hikari").id("dropTestTable").execute();
        new Eql("hikari").id("createTestTable").params(new Timestamp(System.currentTimeMillis())).execute();

        List<Map<String, String>> ret = new Eql("hikari").select("getBeanList").execute();
        System.out.println(ret);

        List<String> strs = new Eql("hikari").select("getStringList").execute();
        System.out.println(strs);


        Integer effectedRows = new Eql("hikari").update("updateBean").params(1, "A1A1").execute();
        assertThat(effectedRows, is(1));

        Bean bean = new Eql("hikari").selectFirst("selectBean").returnType(Bean.class).params(1).execute();
        assertThat(bean, is(notNullValue()));

        Bean bean2 = new Eql("hikari").selectFirst("selectByBean").returnType(Bean.class).params(bean).execute();
        assertThat(bean2, equalTo(bean));

        Bean bean3 = new Eql("hikari").selectFirst("selectByBean2").returnType(Bean.class).params(bean).execute();
        assertThat(bean3, equalTo(bean));

        BeanXX beanXX = new Eql("hikari").selectFirst("selectBean").returnType(BeanXX.class).params(1).execute();
        assertThat(beanXX, is(notNullValue()));
    }

    private Map<String, String> map(String x, String x0, String y, String y0) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(x, x0);
        map.put(y, y0);

        return map;
    }

    public static class Bean {
        private int a;
        private String b;
        private String c;
        private Date d;
        private int e;

        @Override
        public String toString() {
            return "{a:" + a + ",b:" + b + ",c:" + c + ",d:" + (d == null ? d : d.getTime()) + ",e:" + e + "}";
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public Date getD() {
            return d;
        }

        public void setD(Date d) {
            this.d = d;
        }

        public int getE() {
            return e;
        }

        public void setE(int e) {
            this.e = e;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Bean bean = (Bean) o;

            if (a != bean.a) return false;
            if (e != bean.e) return false;
            if (b != null ? !b.equals(bean.b) : bean.b != null) return false;
            if (c != null ? !c.equals(bean.c) : bean.c != null) return false;
            if (d != null ? !d.equals(bean.d) : bean.d != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = a;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            result = 31 * result + (c != null ? c.hashCode() : 0);
            result = 31 * result + (d != null ? d.hashCode() : 0);
            result = 31 * result + e;
            return result;
        }
    }

    // no default constructor and no setter and getters

    static class BeanXX {
        private int a;
        private String b;
        private String c;
        private Date d;
        private int e;

        BeanXX(int a) {
            this.a = a;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Bean bean = (Bean) o;

            if (a != bean.a) return false;
            if (e != bean.e) return false;
            if (b != null ? !b.equals(bean.b) : bean.b != null) return false;
            if (c != null ? !c.equals(bean.c) : bean.c != null) return false;
            if (d != null ? !d.equals(bean.d) : bean.d != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = a;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            result = 31 * result + (c != null ? c.hashCode() : 0);
            result = 31 * result + (d != null ? d.hashCode() : 0);
            result = 31 * result + e;
            return result;
        }
    }

}
