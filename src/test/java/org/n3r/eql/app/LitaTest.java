package org.n3r.eql.app;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.n3r.eql.Eqll;

import java.sql.SQLException;
import java.util.Map;

public class LitaTest {

    @Test
    public void lita() throws SQLException {
        new Eqll().id("LITA").execute();
    }

    @Test
    public void lita2() {
        Map<String, Object> map = Maps.newHashMap();
        Map<String, String> bean = Maps.newHashMap();
        map.put("bean", bean);
        bean.put("name", "huang");

        new Eqll().id("LITA2").params(map).execute();
    }

    @Test
    public void lita3() {
        Map<String, Object> map = Maps.newHashMap();
        OrderInfo bean = new OrderInfo();
        map.put("bean", bean);
        bean.setName("huang");

        new Eqll().id("LITA2").params(map).execute();
    }

    public static class OrderInfo {
        private String name;
        private String payType;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPayType() {
            return payType;
        }

        public void setPayType(String payType) {
            this.payType = payType;
        }
    }
}
