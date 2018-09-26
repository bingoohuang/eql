package org.n3r.eql.liulei;

import com.github.bingoohuang.westid.WestId;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.eqler.EqlerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/*
[root]
MemberCardTest.testAddRecords         46s 67ms
MemberCardTest.testIterateAddRecords  16s 914ms
MemberCardTest.testRawJdbcBatch        7s 289ms
MemberCardTest.testRawJdbc             6s 642ms
MemberCardTest.testInsertMultipleRows  1s 379ms
MemberCardTest.testRawMultipleRows        779ms
 */
public class MemberCardTest {
    final static long cardId = WestId.next();
    final static List<MemberCard> memberCards = createMemberCards();

    @BeforeClass public static void beforeClass() {
        new Eql("dba").execute();
    }

    @Before public void beforeEachTest() {
        new Eql("dba").execute();
    }

    public static final int SIZE = 52 /* 52 weeks per year */ * 2 /* 100 years */;

    @Test public void testAddRecords() {
        for (MemberCard memberCard : memberCards) {
            new Eql("dba").params(memberCard).execute();
        }
        checkSize();
    }

    @Test public void testIterateAddRecords() {
        insertOneTime("testIterateAddRecords");
    }

    @Test public void testInsertMultipleRows() {
        insertOneTime("testInsertMultipleRows");
    }

    @Test public void testInsertMultipleRowsInDao() {
        val dao = EqlerFactory.getEqler(MemberCardDao.class);
        dao.insertMultipleRows(memberCards);
        checkSize();
    }

    @Test public void insertMultipleRowsFengyuReportedNull() {
        val dao = EqlerFactory.getEqler(MemberCardDao.class);
        try {
            dao.insertMultipleRowsFengyuReportedBug(null);
            Assert.fail();
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("You have an error in your SQL syntax;");
        }
        checkSize(0);
    }

    @Test public void insertMultipleRowsFengyuReportedBug() {
        val dao = EqlerFactory.getEqler(MemberCardDao.class);
        dao.insertMultipleRowsFengyuReportedBug(memberCards);
        checkSize();
    }

    @Test public void testInsertMultipleRowsInDao2() {
        val dao = EqlerFactory.getEqler(MemberCardDao.class);
        dao.insertMultipleRows2(memberCards);
        checkSize();
    }

    @Test public void testIterateAddRecordsInDao() {
        val dao = EqlerFactory.getEqler(MemberCardDao.class);
        dao.iterateAddRecords(memberCards);
        checkSize();
    }

    public void insertOneTime(String sqlId) {
        new Eql("dba").id(sqlId).params(memberCards).execute();
        checkSize();
    }

    String sqlPrefix = "insert into member_card_week_times " +
            "(MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME) values";
    String valuePart = "(?, ?, ?, '-1', NOW(), '-1', NOW()),";

    @Test @SneakyThrows public void testRawMultipleRows() {
        StringBuilder sql = new StringBuilder(sqlPrefix);
        List<Object> params = new ArrayList<Object>(SIZE * 3);
        for (MemberCard memberCard : memberCards) {
            sql.append(valuePart);

            params.add(cardId);
            params.add(memberCard.getStartTime());
            params.add(memberCard.getEndTime());
        }
        sql.setLength(sql.length() - 1); // remove last ,

        @Cleanup Connection dba = new Eql("dba").getConnection();
        @Cleanup PreparedStatement ps = dba.prepareStatement(sql.toString());
        for (int i = 0, ii = params.size(); i < ii; ++i) {
            ps.setObject(i + 1, params.get(i));
        }
        ps.executeUpdate();

        dba.commit();

        checkSize();
    }

    @Test @SneakyThrows public void testRawJdbc() {
        @Cleanup Connection dba = new Eql("dba").getConnection();
        @Cleanup PreparedStatement ps = dba.prepareStatement(sql);
        for (MemberCard memberCard : memberCards) {
            setPsParams(ps, memberCard);
            ps.executeUpdate();
        }
        dba.commit();

        checkSize();
    }

    private void checkSize() {
        checkSize(SIZE);
    }

    private void checkSize(int size) {
        int countRecords = new Eql("dba").params(cardId)
                .selectFirst("countRecords").execute();
        assertThat(countRecords).isEqualTo(size);
    }

    String sql = "insert into member_card_week_times " +
            "(MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME) " +
            "values ( ?, ?, ?, '-1', NOW(), '-1', NOW())";

    @Test @SneakyThrows public void testRawJdbcBatch() {
        @Cleanup Connection dba = new Eql("dba").getConnection();
        @Cleanup PreparedStatement ps = dba.prepareStatement(sql);
        for (MemberCard memberCard : memberCards) {
            setPsParams(ps, memberCard);
            ps.addBatch();
        }
        ps.executeBatch();
        dba.commit();

        checkSize();
    }

    @SneakyThrows
    private void setPsParams(PreparedStatement ps, MemberCard memberCard) {
        ps.setLong(1, memberCard.getMbrCardId());
        ps.setTimestamp(2, memberCard.getStartTime());
        ps.setTimestamp(3, memberCard.getEndTime());
    }

    private static List<MemberCard> createMemberCards() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dateTime = formatter.parseDateTime("2016-08-12 00:00:00");

        List<MemberCard> memberCards = new ArrayList<MemberCard>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            MemberCard memberCard = new MemberCard();
            memberCards.add(memberCard);

            memberCard.setMbrCardId(cardId);
            memberCard.setStartTime(new Timestamp(dateTime.plusDays(i * 7).getMillis()));
            memberCard.setEndTime(new Timestamp(dateTime.plusDays(i * 7 + 7).getMillis()));
        }
        return memberCards;
    }

}
