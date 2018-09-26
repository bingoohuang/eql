package org.n3r.eql;

import lombok.Data;
import lombok.val;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by wcy on 2017-04-21.
 */
public class SqlserverTest {

    @Test @Ignore
    public void test1() {
        new Eql("sqlserver").id("create").execute();

        val page = new EqlPage(3, 2);
        List<Bean> beans = new Eql("sqlserver").id("testPage")
                .returnType(Bean.class)
                .limit(page)
                .execute();
        assertThat(page.getTotalRows(), is(10));
        assertThat(beans.size(), is(2));

        new Eql("sqlserver").id("drop").execute();
    }

    @Data
    public static class Bean {
        private int a;
        private String b;
        private String c;
        private int d;
        private int e;
    }
}

