package org.n3r.eql.datetime;

import lombok.Data;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import static com.google.common.truth.Truth.assertThat;

public class SingleDateTimeTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("h2").execute(
                "DROP TABLE IF EXISTS t_localdate; " +
                        "CREATE TABLE t_localdate (id int, local_date date);" +
                        "insert into t_localdate values(100, '2017-10-20') ");
    }


    @EqlerConfig("h2")
    public interface DateTimeDao {
        @Sql("SELECT ##")
        DateTime queryDateTime(DateTime dateTime);

        @Sql("SELECT ##")
        LocalDate queryLocalDate(LocalDate dateTime);

        @Sql("SELECT ##")
        LocalTime queryLocalTime(LocalTime dateTime);
    }

    static DateTimeDao dao = EqlerFactory.getEqler(DateTimeDao.class);

    @Test
    public void jodaDateTime() {
        DateTime dateTime = parseDateAsDateTime("2017-10-20");
        val dt = dao.queryDateTime(dateTime);
        assertThat(dt).isEqualTo(dateTime);
    }

    @Test
    public void jodaLocalDate() {
        LocalDate dateTime = parseDateAsLocalDate("2017-10-20");
        val ld = dao.queryLocalDate(dateTime);
        assertThat(ld).isEqualTo(dateTime);
    }

    @Test
    public void jodaLocalTime() {
        LocalTime dateTime = parseTimeAsLocalTime("14:14:14");
        val lt = dao.queryLocalTime(dateTime);
        assertThat(lt).isEqualTo(dateTime);
    }

    @Data
    public static class JodaBean {
        private DateTime dt;
        private LocalDate localDate;
        private LocalTime localTime;

        public JodaBean(DateTime dt) {
            this.dt = dt;
        }

        public JodaBean(LocalDate localDate) {
            this.localDate = localDate;
        }

        public JodaBean(LocalTime localTime) {
            this.localTime = localTime;
        }
    }

    @EqlerConfig("h2")
    public interface DateTimeDao2 {
        @Sql("SELECT #dt# as dt")
        JodaBean queryDateTime(JodaBean bean);

        @Sql("SELECT id, local_date from t_localdate where #localDate# is not null")
        JodaBean queryLocalDate(JodaBean bean);

        @Sql("SELECT #localTime# as localTime")
        JodaBean queryLocalTime(JodaBean bean);
    }


    static DateTimeDao2 dao2 = EqlerFactory.getEqler(DateTimeDao2.class);

    @Test
    public void test2() {
        DateTime dt1 = parseDateAsDateTime("2017-10-20");
        val dt = dao2.queryDateTime(new JodaBean(dt1));
        assertThat(dt.getDt()).isEqualTo(dt1);
    }

    @Test
    public void jodaLocalDateTest2() {
        LocalDate localDate = parseDateAsLocalDate("2017-10-20");
        val ld = dao2.queryLocalDate(new JodaBean(localDate));
        assertThat(ld.getLocalDate()).isEqualTo(localDate);
    }

    @Test
    public void jodaLocalTimeTest2() {
        LocalTime localTime = parseTimeAsLocalTime("14:14:14");
        val lt = dao2.queryLocalTime(new JodaBean(localTime));
        assertThat(lt.getLocalTime()).isEqualTo(localTime);
    }

    static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    static DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");

    public static LocalTime parseTimeAsLocalTime(String text) {
        return timeFormatter.parseLocalTime(text);
    }

    public static DateTime parseDateAsDateTime(String text) {
        return dateFormatter.parseDateTime(text);
    }

    public static LocalDate parseDateAsLocalDate(String text) {
        return dateFormatter.parseLocalDate(text);
    }
}
