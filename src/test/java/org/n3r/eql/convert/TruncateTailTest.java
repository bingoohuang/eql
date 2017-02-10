package org.n3r.eql.convert;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.n3r.eql.Eql;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/6.
 */
public class TruncateTailTest {
    @Test
    public void test() {
        new Eql("mysql").execute("drop table if exists test_times",
                "create table test_times (id varchar(100), times decimal(11,2)," +
                        " times2 decimal(11,2), times3 decimal(11,2), update_time datetime, sex char(1))",
                "insert into test_times(id, times, times2, times3) value('a111', 123, 234, 2100)");
        ConvertBean bean = new Eql("mysql")
                .returnType(ConvertBean.class)
                .limit(1)
                .execute("select id, times, times2, times3 from test_times");
        ConvertBean expected = new ConvertBean("a111",
                "123", "234.00", "2100", null, false);
        assertThat(bean).isEqualTo(expected);

        String id = RandomStringUtils.randomAlphanumeric(10);
        expected.setId(id);
        expected.setUpdateTime("2017-02-10");
        expected.setSex(true);

        new Eql("mysql").params(expected).execute(
                "insert into test_times(id, times, times2, times3,  update_time, sex) " +
                        "values(#id#, #times#, #times2#,#times3#, #updateTime#, #sex#)");


        ConvertBean bean2 = new Eql("mysql")
                .returnType(ConvertBean.class)
                .params(id)
                .limit(1)
                .execute(
                        "select id, times, times2, times3, update_time, sex " +
                        "from test_times where id = ##");
        assertThat(bean2).isEqualTo(expected);
    }
}
