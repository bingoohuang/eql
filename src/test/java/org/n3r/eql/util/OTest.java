package org.n3r.eql.util;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OTest {
    @Test
    public void populate() {
        OBean oBean = new OBean();
        Map<String, String> map = Maps.newHashMap();
        map.put("name", "bingoo");
        map.put("age", "101");
        map.put("money", "12345");
        map.put("log", "true");
        map.put("x", "yyy");
        O.populate(oBean, map);

        assertThat(oBean.getName(), is(equalTo("bingoo")));
        assertThat(oBean.getX(), is(equalTo("yyy")));
        assertThat(oBean.getAge(), is(equalTo(101)));
        assertThat(oBean.getMoney(), is(equalTo(12345L)));
        assertThat(oBean.isLog(), is(equalTo(true)));
    }

    public static class OBean {
        String name;
        int age;
        long money;
        boolean log;
        String x;

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }

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

        public long getMoney() {
            return money;
        }

        public void setMoney(long money) {
            this.money = money;
        }

        public boolean isLog() {
            return log;
        }

        public void setLog(boolean log) {
            this.log = log;
        }
    }
}
