package org.n3r.eql.datetime;

import lombok.Data;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
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

        @Sql("SELECT ##")
        LocalDate queryLocalDate(LocalDate dateTime);

        @Sql("SELECT ##")
        LocalTime queryLocalTime(LocalTime dateTime);
    }

    @Data
    public static class JodaBean {
        private DateTime dt;
        private LocalDate ld;
        private LocalTime lt;
    }

    @Test
    public void test1() {
        val dao = EqlerFactory.getEqler(DateTimeDao.class);
        val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        val dateTime = dateTimeFormatter.parseDateTime("2017-10-20");
        val quered = dao.queryDateTime(dateTime);
        assertThat(quered.toString(dateTimeFormatter)).isEqualTo("2017-10-20");
    }

    @Test
    public void jodaLocalDateTest() {
        val dao = EqlerFactory.getEqler(DateTimeDao.class);
        val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        val dateTime = dateTimeFormatter.parseLocalDate("2017-10-20");
        val quered = dao.queryLocalDate(dateTime);
        assertThat(quered.toString(dateTimeFormatter)).isEqualTo("2017-10-20");
    }

    @Test
    public void jodaLocalTimeTest() {
        val dao = EqlerFactory.getEqler(DateTimeDao.class);
        val dateTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
        val dateTime = dateTimeFormatter.parseLocalTime("14:14:14");
        val quered = dao.queryLocalTime(dateTime);
        assertThat(quered.toString(dateTimeFormatter)).isEqualTo("14:14:14");
    }


    @EqlerConfig("h2")
    public interface DateTimeDao2 {
        @Sql("SELECT ## as dt")
        JodaBean queryDateTime(DateTime dateTime);

        @Sql("SELECT ## as ld")
        JodaBean queryLocalDate(LocalDate dateTime);

        @Sql("SELECT ## as lt")
        JodaBean queryLocalTime(LocalTime dateTime);
    }


    @Test
    public void test2() {
        val dao = EqlerFactory.getEqler(DateTimeDao2.class);
        val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        val dateTime = dateTimeFormatter.parseDateTime("2017-10-20");
        val quered = dao.queryDateTime(dateTime);
        assertThat(quered.getDt().toString(dateTimeFormatter)).isEqualTo("2017-10-20");
    }

    @Test
    public void jodaLocalDateTest2() {
        val dao = EqlerFactory.getEqler(DateTimeDao2.class);
        val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        val dateTime = dateTimeFormatter.parseLocalDate("2017-10-20");
        val quered = dao.queryLocalDate(dateTime);
        assertThat(quered.getLd().toString(dateTimeFormatter)).isEqualTo("2017-10-20");
    }

    @Test
    public void jodaLocalTimeTest2() {
        val dao = EqlerFactory.getEqler(DateTimeDao2.class);
        val dateTimeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
        val dateTime = dateTimeFormatter.parseLocalTime("14:14:14");
        val quered = dao.queryLocalTime(dateTime);
        assertThat(quered.getLt().toString(dateTimeFormatter)).isEqualTo("14:14:14");
    }
}
