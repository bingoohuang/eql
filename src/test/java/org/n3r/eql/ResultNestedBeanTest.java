package org.n3r.eql;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResultNestedBeanTest {
    @Test
    public void test() {
        Object result = new Eql("mysql").returnType(OuterBean.class).limit(1).execute();
        assertThat(result.toString(), is("OuterBean{name='bingoohuang', bean=InnerBean{addr='gongjianfang'}}"));
    }

    @Test
    public void test1() {
        Object result = new Eql("mysql").id("test").returnType(OuterBean1.class).limit(1).execute();
        assertThat(result.toString(), is("OuterBean1{name='bingoohuang', bean=InnerBean1{addr='gongjianfang'}}"));
    }

    @Test
    public void test3() {
        Object result = new Eql("mysql").id("test").returnType(OuterBean2.class).limit(1).execute();
        assertThat(result.toString(), is("OuterBean2{name='bingoohuang', bean={addr=gongjianfang}}"));
    }

    public static class OuterBean2 {
        private String name;
        private Map bean;

        @Override
        public String toString() {
            return "OuterBean2{" +
                    "name='" + name + '\'' +
                    ", bean=" + bean +
                    '}';
        }
    }


    public static class OuterBean1 {
        private String name;
        private InnerBean1 bean;

        @Override
        public String toString() {
            return "OuterBean1{" +
                    "name='" + name + '\'' +
                    ", bean=" + bean +
                    '}';
        }
    }

    public static class InnerBean1 {
        private String addr;

        @Override
        public String toString() {
            return "InnerBean1{" +
                    "addr='" + addr + '\'' +
                    '}';
        }
    }

    public static class OuterBean {
        private String name;
        private InnerBean bean;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public InnerBean getBean() {
            return bean;
        }

        public void setBean(InnerBean bean) {
            this.bean = bean;
        }

        @Override
        public String toString() {
            return "OuterBean{" +
                    "name='" + name + '\'' +
                    ", bean=" + bean +
                    '}';
        }
    }

    public static class InnerBean {
        private String addr;

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        @Override
        public String toString() {
            return "InnerBean{" +
                    "addr='" + addr + '\'' +
                    '}';
        }
    }


}
