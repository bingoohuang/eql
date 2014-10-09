package org.n3r.eql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Ignore;
import org.junit.Test;
import org.n3r.eql.diamond.Dql;
import org.n3r.eql.map.EqlRun;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ForTest {
    @Test
    public void test1() {
        new Eql().id("dropTestTable").execute();
        new Eql().id("createTestTable").params(new Timestamp(1383122146000l)).execute();

        Map<String, Object> map = Maps.newHashMap();
        map.put("list", ImmutableList.of("a", "b", "x"));

        String str = new Eql().selectFirst("for1").params(map).execute();
        assertThat(str, is("x"));
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

    @Test @Ignore
    public void queryGoodsAttrsBaseInfo() {
        List<Map> goodsAttrs = new Dql().params(of("productId", "3001")).execute();
        new Dql().select("dkr").params(of("productId", "3001", "goodsAttrs", goodsAttrs)).execute();
    }
}
