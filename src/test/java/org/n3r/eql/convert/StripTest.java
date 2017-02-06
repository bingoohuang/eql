package org.n3r.eql.convert;

import org.junit.Test;
import org.n3r.eql.Eql;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/6.
 */
public class StripTest {
    @Test
    public void test() {
        new Eql("mysql").execute("drop table if exists test_times",
                "create table test_times (id varchar(100), times decimal(11,2), times2 decimal(11,2))",
                "insert into test_times value('a111', 123, 234)");
        ConvertBean bean = new Eql("mysql")
                .returnType(ConvertBean.class)
                .limit(1)
                .execute("select id, times, times2 from test_times");
        assertThat(bean).isEqualTo(new ConvertBean("a111", "123", "234.00"));
    }
}
