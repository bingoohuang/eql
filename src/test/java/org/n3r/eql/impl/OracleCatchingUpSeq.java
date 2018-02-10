package org.n3r.eql.impl;

import lombok.SneakyThrows;
import lombok.Synchronized;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.Dynamic;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;
import org.n3r.eql.util.EqlUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
-- 创建内存表
CREATE TABLE MY_SEQ(
  NAME VARCHAR2(100) PRIMARY KEY,
  SEQ1 NUMBER DEFAULT 0 NOT NULL,
  SEQ2 NUMBER DEFAULT 0 NOT NULL,
  SEQ3 NUMBER DEFAULT 0 NOT NULL
) STORAGE(BUFFER_POOL KEEP);

-- 增加测试序列
INSERT INTO MY_SEQ(NAME) VALUES('XXX');
*/
public class OracleCatchingUpSeq {
    @EqlerConfig("orcl")
    public interface SeqDao {
//        @SqlOptions(onErr = OnErr.Resume)
//        @Sql({"DROP TABLE MY_SEQ",
//                "CREATE TABLE MY_SEQ(" +
//                        "  NAME VARCHAR2(100) PRIMARY KEY," +
//                        "  SEQ1 NUMBER DEFAULT 0 NOT NULL," +
//                        "  SEQ2 NUMBER DEFAULT 0 NOT NULL," +
//                        "  SEQ3 NUMBER DEFAULT 0 NOT NULL" +
//                        ") STORAGE(BUFFER_POOL KEEP)"})
//        void setup();

        @Sql("SELECT NAME FROM MY_SEQ")
        List<String> selectSeqNames();

        @Sql("INSERT INTO MY_SEQ(NAME) VALUES(##)")
        void createSeq(String seqName);

        @Sql("{CALL UPDATE MY_SEQ SET SEQ1 = SEQ1 + 1 WHERE NAME = ## RETURNING SEQ1 INTO #:OUT(Long)# }")
        long nextSeq1(String seqName);

        @Sql("{CALL UPDATE MY_SEQ SET $1$ = $1$ + 1 WHERE NAME = ## AND $1$ < SEQ1 RETURNING $1$ INTO #:OUT(Long)# }")
        Long nextSeqX(String seqName, @Dynamic String seqField);
    }

    static SeqDao seqDao = EqlerFactory.getEqler(SeqDao.class);
    static Set<String> seqNames = new HashSet<String>(seqDao.selectSeqNames());

    /**
     * 获取SEQ1（主序列），当名字不存在时，增加名字。
     *
     * @param seqName 序列名称
     * @return SEQ1下一个取值(从1开始)
     */
    public static long nextSeq1(String seqName) {
        if (!seqNames.contains(seqName)) createSeq(seqName);

        return seqDao.nextSeq1(seqName);
    }

    /**
     * 获取SEQ2（追赶序列）。
     *
     * @param seqName 序列名称
     * @return SEQ2下一个取值(从1开始)，返回0表示序列不存在，或者已经追上主序列
     */
    public static long nextSeq2(String seqName) {
        return nextSeqX(seqName, "SEQ2");
    }

    /**
     * 获取SEQ3（追赶序列）。
     *
     * @param seqName 序列名称
     * @return SEQ3下一个取值(从1开始)，返回0表示序列不存在，或者已经追上主序列
     */
    public static long nextSeq3(String seqName) {
        return nextSeqX(seqName, "SEQ3");
    }

    private static long nextSeqX(String seqName, String seqField) {
        Long seqX = seqDao.nextSeqX(seqName, seqField);
        return seqX == null ? 0 : seqX.longValue();
    }


    @Synchronized("seqNames") @SneakyThrows
    private static void createSeq(String seqName) {
        try {
            seqDao.createSeq(seqName);
        } catch (Exception ex) {
            if (!EqlUtils.isConstraintViolation(ex)) throw ex;
            // java.sql.SQLException: ORA-00001: 违反唯一约束条件 (SYSTEM.SYS_C007599)
            // 说明已经被人抢先增加了
            // if (!ex.toString().contains("ORA-00001")) throw ex;
        }
        seqNames.add(seqName);
    }

    public static void main(String[] args) {
        System.out.println(nextSeq2("ppp"));
        System.out.println(nextSeq1("XXX"));
        System.out.println(nextSeq2("XXX"));
        System.out.println(nextSeq3("XXX"));
    }
}
