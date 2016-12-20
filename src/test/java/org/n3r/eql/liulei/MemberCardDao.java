package org.n3r.eql.liulei;

import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;
import org.n3r.eql.eqler.annotations.SqlOptions;

import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/20.
 */
@EqlerConfig(value = "dba")
public interface MemberCardDao {
    @Sql("insert into member_card_week_times (MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME) " +
            "values\n" +
            "-- for item=card index=index collection=_1 separator=,\n" +
            "('#card.mbrCardId#', '#card.startTime#', '#card.endTime#', -1, NOW(), -1, NOW())\n" +
            "-- end")
    void insertMultipleRows(List<MemberCard> memberCards);

    @Sql("insert into member_card_week_times (MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME) " +
            "values " +
            "/* for item=card index=index collection=_1 separator=, */ " +
            "('#card.mbrCardId#', '#card.startTime#', '#card.endTime#', -1, NOW(), -1, NOW()) " +
            "/* end */")
    void insertMultipleRows2(List<MemberCard> memberCards);

    @SqlOptions(iterate = true)
    @Sql("insert into member_card_week_times (MBR_CARD_ID, START_TIME, END_TIME, TIMES, UPDATE_TIME, AVAIL_TIMES, CREATE_TIME) " +
            "values ('#mbrCardId#', '#startTime#', '#endTime#', -1, NOW(), -1, NOW())")
    void iterateAddRecords(List<MemberCard> memberCards);
}
