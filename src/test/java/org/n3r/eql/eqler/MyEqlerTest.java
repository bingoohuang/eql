package org.n3r.eql.eqler;

import com.google.common.collect.Maps;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.EqlPage;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MyEqlerTest {
    static MyEqler myEqler;

    @BeforeClass
    public static void beforeClass() {
        myEqler = EqlerFactory.getEqler(MyEqler.class);
    }

    @Test
    public void queryOne() {
        String one = myEqler.queryOne();
        assertThat(one, is(equalTo("1")));
    }

    @Test
    public void queryTwo() {
        String two = myEqler.queryTwo();
        assertThat(two, is(equalTo("2")));
    }

    @Test
    public void queryThree() {
        assertThat(myEqler.queryThree(), is(3));

    }

    @Test
    public void queryFour() {
        assertThat(myEqler.queryFour(), is(4L));

    }

    @Test
    public void queryBoolean() {
        assertThat(myEqler.queryTrue(), is(true));
        assertThat(myEqler.queryFalse(), is(false));
    }

    @Test
    public void queryById() {
        assertThat(myEqler.queryById("a"), is(equalTo("a")));
    }

    @Test
    public void queryByMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("a", "bb");
        assertThat(myEqler.queryByMap(map), is(equalTo("bb")));
    }

    @Test
    public void queryBean() {
        MyEqlerBean bean = myEqler.queryBean("bb");

        assertThat(bean, is(equalTo(new MyEqlerBean("bb"))));
    }

    @Test
    public void queryBeanX() {
        MyEqlerBean bean = myEqler.queryBeanX("bb");

        assertThat(bean, is(equalTo(new MyEqlerBean("bb"))));
    }

    @Test
    public void queryBeans() {
        List<MyEqlerBean> beans = myEqler.queryBeans("bb");
        assertThat(beans.size(), is(1));

        MyEqlerBean bean = beans.get(0);
        assertThat(bean, is(equalTo(new MyEqlerBean("bb"))));
    }

    @Test
    public void queryMoreBeans() {
        EqlPage eqlPage = new EqlPage(0, 2);
        List<MyEqlerBean> beans = myEqler.queryMoreBeans(1, eqlPage, 2);
        assertThat(beans.size(), is(2));

        assertThat(beans.get(0), is(equalTo(new MyEqlerBean("u"))));
        assertThat(beans.get(1), is(equalTo(new MyEqlerBean("x"))));
    }

    @Test
    public void queryDirectSql() {
        String one = myEqler.queryDirectSql();
        assertThat(one, is(equalTo("1")));
    }

    @Test
    public void queryByMapNamed() {
        Map map = myEqler.queryByMap(123L, "M456", 789, "Name10");
        assertThat(map.size(), is(4));
        assertThat((Long) map.get("userId"), is(equalTo(123L)));
        assertThat((String) map.get("name"), is(equalTo("Name10")));
        assertThat((Long) map.get("id"), is(equalTo(789L)));
        assertThat((String) map.get("merchantId"), is(equalTo("M456")));
    }
}
