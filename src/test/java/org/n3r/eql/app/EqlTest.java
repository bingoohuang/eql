package org.n3r.eql.app;


import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.n3r.eql.Eql;

public class EqlTest {

    @Test
    public void testEql() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(System.currentTimeMillis())).execute();

        QueryBean queryBean = new QueryBean("3400000", new OrderInfo("340001", null));
        List<Map> retList = new Eql().select("getOrders").params(queryBean).execute();
        System.out.println(retList);
    }

    static class QueryBean {
        String merchantId;
        OrderInfo orderInfo;

        public QueryBean(String merchantId, OrderInfo orderInfo) {
            this.merchantId = merchantId;
            this.orderInfo = orderInfo;
        }
    }

    class OrderInfo {
        String staffCode;
        String staffId;

        public OrderInfo(String staffCode, String staffId) {
            this.staffCode = staffCode;
            this.staffId = staffId;
        }
    }
}
