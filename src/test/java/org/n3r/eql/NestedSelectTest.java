package org.n3r.eql;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class NestedSelectTest {
    @Test
    public void test1() {
        List<Object> list = new Eql().id("bug0").execute();
        assertEquals(1, list.size());

        List<Bean> beans = new Eql().id("bug0").returnType(Bean.class).execute();
        assertEquals(1, beans.size());
        Bean bean = beans.get(0);
        assertEquals("a", bean.getName());
        assertEquals(1, bean.getAge());
    }

    public static class Bean {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
