package org.n3r.eql.liulei;


import lombok.Cleanup;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.idworker.Id;

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
 */
public class MemberCardTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("dba").execute();
    }

    @Before
    public void beforeEachTest() {
        new Eql("dba").execute();
    }

    public static final int SIZE = 5200;

    @Test
    public void testIterateAddRecords() {
        String strCardId = "" + Id.next();
        List<MemberCard> memberCards = createMemberCards(strCardId);

        new Eql("dba").params(memberCards).execute();
        int countRecords = new Eql("dba").params(strCardId).selectFirst("countRecords").execute();
        assertThat(countRecords).isEqualTo(SIZE);
    }

    @Test
    public void testAddRecords() {
        String strCardId = "" + Id.next();
        List<MemberCard> memberCards = createMemberCards(strCardId);

        for (MemberCard memberCard : memberCards) {
            new Eql("dba").params(memberCard).execute();
        }
        int countRecords = new Eql("dba").params(strCardId).selectFirst("countRecords").execute();
        assertThat(countRecords).isEqualTo(SIZE);
    }

    @Test
    public void testInsertMultipleRows() {
        String strCardId = "" + Id.next();
        List<MemberCard> memberCards = createMemberCards(strCardId);

        new Eql("dba").params(memberCards).execute();
        int countRecords = new Eql("dba").params(strCardId).selectFirst("countRecords").execute();
        assertThat(countRecords).isEqualTo(SIZE);
    }

    @Test @SneakyThrows
    public void testRawJdbc() {
        String strCardId = "" + Id.next();
        List<MemberCard> memberCards = createMemberCards(strCardId);
        @Cleanup Connection dba = new Eql("dba").getConnection();
        @Cleanup PreparedStatement ps = dba.prepareStatement(sql);
        for (MemberCard memberCard : memberCards) {
            ps.setString(1, strCardId);
            ps.setTimestamp(2, memberCard.getStartTime());
            ps.setTimestamp(3, memberCard.getEndTime());
            ps.executeUpdate();
        }
        dba.commit();

        int countRecords = new Eql("dba").params(strCardId).selectFirst("countRecords").execute();
        assertThat(countRecords).isEqualTo(SIZE);
    }

    String sql = "insert into member_card_week_times " +
            "(MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME) " +
            "values ( ?, ?, ?, '-1', NOW(), '-1', NOW())";

    @Test @SneakyThrows
    public void testRawJdbcBatch() {
        String strCardId = "" + Id.next();
        List<MemberCard> memberCards = createMemberCards(strCardId);
        @Cleanup Connection dba = new Eql("dba").getConnection();
        @Cleanup PreparedStatement ps = dba.prepareStatement(sql);
        for (MemberCard memberCard : memberCards) {
            ps.setString(1, strCardId);
            ps.setTimestamp(2, memberCard.getStartTime());
            ps.setTimestamp(3, memberCard.getEndTime());
            ps.addBatch();
        }
        ps.executeBatch();
        dba.commit();

        int countRecords = new Eql("dba").params(strCardId).selectFirst("countRecords").execute();
        assertThat(countRecords).isEqualTo(SIZE);
    }

    private List<MemberCard> createMemberCards(String strCardId) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dateTime = formatter.parseDateTime("2016-08-12 00:00:00");

        List<MemberCard> memberCards = new ArrayList<MemberCard>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            MemberCard memberCard = new MemberCard();
            memberCard.setMbrCardId(strCardId);

            DateTime startTime = dateTime.plusDays(i * 7);

            memberCard.setStartTime(new Timestamp(dateTime.getMillis()));
            memberCard.setEndTime(new Timestamp(startTime.plus(7).getMillis()));
            memberCards.add(memberCard);
        }
        return memberCards;
    }

}
