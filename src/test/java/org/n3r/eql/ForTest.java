package org.n3r.eql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
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

        columnDefs.add(ImmutableMap.of("name", "bingoovalue", "search", "searchvalue"));
        sortingColumnDefs.add(ImmutableMap.of("name", "bingoovalue", "sortDirection", "desc"));

        new Eql().id("findPageAddressInfo").params(map).evaluate();

    }
}
