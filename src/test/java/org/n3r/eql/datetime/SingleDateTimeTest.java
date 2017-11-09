package org.n3r.eql.datetime;

import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import static com.google.common.truth.Truth.assertThat;

public class SingleDateTimeTest {
    @EqlerConfig("h2")
    public interface DateTimeDao {
        @Sql("SELECT ##")
        DateTime queryDateTime(DateTime dateTime);
    }

    @Test
    public void test1() {
        val dao = EqlerFactory.getEqler(DateTimeDao.class);
        val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        val dateTime = dateTimeFormatter.parseDateTime("2017-10-20");
        val quered = dao.queryDateTime(dateTime);
        assertThat(quered.toString(dateTimeFormatter)).isEqualTo("2017-10-20");
    }
}
