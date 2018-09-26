package org.n3r.eql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.n3r.eql.map.EqlRun;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ForTest {
    @Test
    public void test1() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("list", ImmutableList.of("a", "b", "x"));

        String str = new Eql().selectFirst("for1").params(map).execute();
        assertThat(str, is("x"));

        Map<String, Object> map2 = Maps.newHashMap();
        map2.put("map", ImmutableMap.of("a", "b", "x", "y"));

        String str2 = new Eql().selectFirst("for2").params(map2).execute();
        assertThat(str2, is("x"));
    }

    @Test
    public void test2() {
        List<Object> columnDefs = Lists.newArrayList();
        List<Object> sortingColumnDefs = Lists.newArrayList();
        Map<String, Object> map = Maps.newHashMap();
        map.put("columnDefs", columnDefs);
        map.put("sortingColumnDefs", sortingColumnDefs);

        columnDefs.add(of("name", "bingoovalue", "search", "searchvalue"));
        sortingColumnDefs.add(of("name", "bingoovalue", "sortDirection", "desc"));

        List<EqlRun> eqlRuns = new Eql().id("findPageAddressInfo").params(map).evaluate();
        EqlRun eqlRun = eqlRuns.get(0);
        assertEquals("SELECT id, NAME, email, phone FROM MVC_ADDRESS_INFO WHERE 1 = 1  " +
                        "AND $columnDefs[0].name$ LIKE concat('%',?, '%') " +
                        "order by $sortingColumnDefs[0].name$ $sortingColumnDefs[0].sortDirection$",
                eqlRun.getPrintSql());
    }

    @Test
    public void queryGoodsAttrsBaseInfo() {
        new Eql("mysql").id("dkt_setup").execute();

        List goodsAttrs = Lists.newArrayList(ImmutableMap.of("ATTR_ID", "2000"), ImmutableMap.of("ATTR_ID", "2001"));
        ImmutableMap<String, List> of = of("goodsAttrs", goodsAttrs);
        Eql eql = new Eql("mysql").select("dkr").params(of).dynamics(of);
        eql.execute();
        EqlRun eqlRun = eql.getEqlRuns().get(0);
        assertEquals("SELECT * FROM  ( SELECT V.ATTR_VALUE_ID AS ID0 ,V.ATTR_VALUE_NAME AS NAME0 " +
                        "FROM TEST_ATTR_VALUE V WHERE V.ATTR_ID = 2000 ) V0 " +
                        "JOIN" +
                        "( SELECT V.ATTR_VALUE_ID AS ID1 ,V.ATTR_VALUE_NAME AS NAME1 " +
                        "FROM TEST_ATTR_VALUE V WHERE V.ATTR_ID = 2001 ) V1",
                eqlRun.getPrintSql());
    }

    @Test
    public void queryOgnlSelection1() {
        new Eql("mysql").id("dkt_setup").execute();

        List goodsAttrs = Lists.newArrayList(ImmutableMap.of("ATTR_ID", "2000", "attrType", "1"), ImmutableMap.of("ATTR_ID", "2001", "attrType", "0"));
        ImmutableMap<String, List> of = of("goodsAttrs", goodsAttrs);
        Eql eql = new Eql("mysql").select("haoye").params(of).dynamics(of);
        eql.execute();
        EqlRun eqlRun = eql.getEqlRuns().get(0);
        assertEquals("SELECT  MAX(case V.ATTR_ID WHEN ? THEN V.ATTR_ID ELSE NULL END) ATTR_ID0 ," +
                        "MAX(case V.ATTR_ID WHEN ? THEN V.ATTR_VALUE_ID ELSE NULL END) ATTR_VALUE_ID0 " +
                        "FROM TEST_ATTR_VALUE V GROUP BY V.ATTR_ID",
                eqlRun.getPrintSql());
    }

    @Test
    public void queryOgnlSelection2() {
        new Eql("mysql").id("dkt_setup").execute();

        List goodsAttrs = Lists.newArrayList(ImmutableMap.of("ATTR_ID", "2000", "attrType", "1"), ImmutableMap.of("ATTR_ID", "2001", "attrType", "1"));
        ImmutableMap<String, List> of = of("goodsAttrs", goodsAttrs);
        Eql eql = new Eql("mysql").select("haoye").params(of).dynamics(of);
        eql.execute();
        EqlRun eqlRun = eql.getEqlRuns().get(0);
        assertEquals("SELECT  MAX(case V.ATTR_ID WHEN ? THEN V.ATTR_ID ELSE NULL END) ATTR_ID0 " +
                        ",MAX(case V.ATTR_ID WHEN ? THEN V.ATTR_VALUE_ID ELSE NULL END) ATTR_VALUE_ID0 " +
                        ",MAX(case V.ATTR_ID WHEN ? THEN V.ATTR_ID ELSE NULL END) ATTR_ID1 " +
                        ",MAX(case V.ATTR_ID WHEN ? THEN V.ATTR_VALUE_ID ELSE NULL END) ATTR_VALUE_ID1 " +
                        "FROM TEST_ATTR_VALUE V GROUP BY V.ATTR_ID",
                eqlRun.getPrintSql());
    }
}
