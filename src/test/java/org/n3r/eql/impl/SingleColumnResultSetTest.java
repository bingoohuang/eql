package org.n3r.eql.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SingleColumnResultSetTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").execute();
    }

    @Test
    public void test1() {
        List<String> ids = new Eql("mysql").returnType(String.class).execute();
        assertThat(ids, equalTo(Arrays.asList("2012", "2013", "2014")));

        List<Integer> shortIds = new Eql("mysql").returnType(int.class).execute();
        assertThat(shortIds, equalTo(Arrays.asList(2012, 2013, 2014)));

        List<IntIdBean> intIdBeans = new Eql("mysql").returnType(IntIdBean.class).execute();
        assertThat(intIdBeans, equalTo(Arrays.asList(new IntIdBean(2012), new IntIdBean(2013), new IntIdBean(2014))));

        List<StrIdBean> strIdBeans = new Eql("mysql").returnType(StrIdBean.class).execute();
        assertThat(strIdBeans, equalTo(Arrays.asList(new StrIdBean("2012"), new StrIdBean("2013"), new StrIdBean("2014"))));
    }

    public static class StrIdBean {
        String id;

        public StrIdBean() {
        }

        public StrIdBean(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StrIdBean strIdBean = (StrIdBean) o;

            if (id != null ? !id.equals(strIdBean.id) : strIdBean.id != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }
    public static class IntIdBean {
        private int id;

        public IntIdBean() {
        }

        public IntIdBean(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IntIdBean intIdBean = (IntIdBean) o;

            if (id != intIdBean.id) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }
}
