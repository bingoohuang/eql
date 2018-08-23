package org.n3r.eql.map;

import com.google.auto.service.AutoService;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.n3r.eql.util.BlackcatUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Set;

@AutoService(MapperFactory.class)
public class DefaultMapperFactory implements MapperFactory {
    public static final boolean HasJodaDateTime = BlackcatUtils.classExists("org.joda.time.DateTime");

    @Override public void addToDbMapper(Set<ToDbMapper> mappers) {
        if (HasJodaDateTime) {
            mappers.add(new DateTimeToDbMapper());
            mappers.add(new LocalDateToDbMapper());
            mappers.add(new LocalTimeToDbMapper());
        }
    }

    @Override public void addFromDbMapper(Set<FromDbMapper> mappers) {
        if (HasJodaDateTime) {
            mappers.add(new DateTimeFromDbMapper());
            mappers.add(new LocalDateFromDbMapper());
            mappers.add(new LocalTimeFromDbMapper());
        }

        mappers.add(new ClobFromDbMapper());
        mappers.add(new BlobFromDbMapper());
        mappers.add(new BigDecimalFromDbMapper());
        mappers.add(new TimeFromDbMapper());
        mappers.add(new TimestampFromDbMapper());
        mappers.add(new UtilDateFromDbMapper());
    }


    public static class DateTimeToDbMapper implements ToDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return clazz == DateTime.class;
        }

        @Override public Object map(Object obj) {
            return new Timestamp(((DateTime) obj).getMillis());
        }
    }

    public static class LocalDateToDbMapper implements ToDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return clazz == LocalDate.class;
        }

        @Override public Object map(Object obj) {
            val d = (LocalDate) obj;
            return Date.valueOf(java.time.LocalDate.of(d.getYear(), d.getMonthOfYear(), d.getDayOfMonth()));
        }
    }

    public static class LocalTimeToDbMapper implements ToDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return clazz == LocalTime.class;
        }

        @Override public Object map(Object obj) {
            val t = (LocalTime) obj;
            return Time.valueOf(java.time.LocalTime.of(t.getHourOfDay(), t.getMinuteOfHour(), t.getSecondOfMinute(), t.getMillisOfSecond()));
        }
    }

    public static class DateTimeFromDbMapper implements FromDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return clazz == DateTime.class;
        }

        @Override public Object map(RsAware rs, int index) throws SQLException {
            val ts = rs.getTimestamp(index);
            return ts == null ? null : new DateTime(ts.getTime());
        }
    }

    public static class LocalDateFromDbMapper implements FromDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return clazz == LocalDate.class;
        }

        @Override public Object map(RsAware rs, int index) throws SQLException {
            val date = rs.getDate(index);
            if (date == null) return null;

            val c = Calendar.getInstance();
            c.setTime(date);
            return new LocalDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
        }
    }

    public static class LocalTimeFromDbMapper implements FromDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return clazz == LocalTime.class;
        }

        @Override public Object map(RsAware rs, int index) throws SQLException {
            val time = rs.getTime(index);
            if (time == null) return null;

            val c = Calendar.getInstance();
            c.setTime(time);
            return new LocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND));
        }
    }

    public static class ClobFromDbMapper implements FromDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return Clob.class == clazz;
        }

        @Override public Object map(RsAware rs, int index) throws SQLException {
            return rs.getClob(index);
        }
    }

    public static class BlobFromDbMapper implements FromDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return Blob.class == clazz;
        }

        @Override public Object map(RsAware rs, int index) throws SQLException {
            return rs.getBlob(index);
        }
    }

    public static class BigDecimalFromDbMapper implements FromDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return BigDecimal.class == clazz;
        }

        @Override public Object map(RsAware rs, int index) throws SQLException {
            return rs.getBigDecimal(index);
        }
    }

    public static class TimeFromDbMapper implements FromDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return Time.class == clazz;
        }

        @Override public Object map(RsAware rs, int index) throws SQLException {
            return rs.getTime(index);
        }
    }


    public static class TimestampFromDbMapper implements FromDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return Timestamp.class == clazz;
        }

        @Override public Object map(RsAware rs, int index) throws SQLException {
            return rs.getTimestamp(index);
        }
    }

    public static class UtilDateFromDbMapper implements FromDbMapper {
        @Override public boolean support(Class<?> clazz) {
            return java.util.Date.class == clazz;
        }

        @Override public Object map(RsAware rs, int index) throws SQLException {
            return rs.getTimestamp(index);
        }
    }
}
