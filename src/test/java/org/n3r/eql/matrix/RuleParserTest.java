package org.n3r.eql.matrix;

import org.junit.Test;
import org.n3r.eql.matrix.func.PrefixFunction;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RuleParserTest {
    /**
     * 1. 取订单前两位，直接映射到对应的数据库。
     * alias(pre, org.n3r.eql.matrix.func.PrefixFunction)
     * rule(1) pre(.order.id, 2) map(db$)
     */
    @Test
    public void testPrefix() {
        RuleParser ruleParser = new RuleParser();
        RulesSet rulesSet = ruleParser.parse("alias(pre, " + PrefixFunction.class.getName() + ")\n"
                + "rule(1) pre(.order.id, 2) map(db$)");

        RealPartition realPartition = rulesSet.find(new MatrixTableFieldValue("order", "id", "1011"));
        assertThat(realPartition.databaseName, is("db10"));
        assertThat(realPartition.tableName, is(""));

        realPartition = rulesSet.find(new MatrixTableFieldValue("order", "id", "2011"));
        assertThat(realPartition.databaseName, is("db20"));
        assertThat(realPartition.tableName, is(""));
    }

    @Test
    public void testPostfix() {
        RuleParser ruleParser = new RuleParser();
        RulesSet rulesSet = ruleParser.parse("rule(1) post(.order.id, 2) map(db$)");

        RealPartition realPartition = rulesSet.find(new MatrixTableFieldValue("order", "id", "1011"));
        assertThat(realPartition.databaseName, is("db11"));
        assertThat(realPartition.tableName, is(""));

        realPartition = rulesSet.find(new MatrixTableFieldValue("order", "id", "2012"));
        assertThat(realPartition.databaseName, is("db12"));
        assertThat(realPartition.tableName, is(""));
    }

    @Test
    public void testMiddle() {
        RuleParser ruleParser = new RuleParser();
        RulesSet rulesSet = ruleParser.parse("rule(1) mid(.order.id, 1, 2) map(db$)");

        RealPartition realPartition = rulesSet.find(new MatrixTableFieldValue("order", "id", "1011"));
        assertThat(realPartition.databaseName, is("db01"));
        assertThat(realPartition.tableName, is(""));

        realPartition = rulesSet.find(new MatrixTableFieldValue("order", "id", "2032"));
        assertThat(realPartition.databaseName, is("db03"));
        assertThat(realPartition.tableName, is(""));
    }

    @Test(expected = Exception.class)
    public void testMiddleEx1() {
        RuleParser ruleParser = new RuleParser();
        RulesSet rulesSet = ruleParser.parse("rule(1) mid(.order.id, 1, 2) map(db$)");

        rulesSet.find(new MatrixTableFieldValue("order", "id", "10"));
    }

    /**
     * 2. 取模，映射到不同的库。
     * rule(2) mod(.order.id, 3) map(0:dba, 1:dbb, dbc)
     */
    @Test
    public void test2() {
        RuleParser ruleParser = new RuleParser();
        RulesSet rulesSet = ruleParser.parse("rule(2) mod(.order.id, 3) map(0:dba, 1:dbb, dbc)");

        RealPartition realPartition = rulesSet.find(new MatrixTableFieldValue("order", "id", "001"));
        assertThat(realPartition.databaseName, is("dbb"));
        assertThat(realPartition.tableName, is(""));

        realPartition = rulesSet.find(new MatrixTableFieldValue("order", "id", "002"));
        assertThat(realPartition.databaseName, is("dbc"));
        assertThat(realPartition.tableName, is(""));
    }

    /**
     * 3. 分表，女的到person_f表，男的到person_m表。
     * rule(3) val(.person.sex) map(0:.person_f, .person_m)
     */
    @Test
    public void test3() {
        RuleParser ruleParser = new RuleParser();
        RulesSet rulesSet = ruleParser.parse("rule(3) val(.person.sex) map(0:.person_f, .person_m)");

        RealPartition realPartition = rulesSet.find(new MatrixTableFieldValue("person", "sex", "0"));
        assertThat(realPartition.databaseName, is(""));
        assertThat(realPartition.tableName, is("person_f"));

        realPartition = rulesSet.find(new MatrixTableFieldValue("person", "sex", "1"));
        assertThat(realPartition.databaseName, is(""));
        assertThat(realPartition.tableName, is("person_m"));

        realPartition = rulesSet.find(new MatrixTableFieldValue("person", "sex", "2"));
        assertThat(realPartition.databaseName, is(""));
        assertThat(realPartition.tableName, is("person_m"));
    }

    /**
     * 4. 分区分表，按性别区分，女的到a库，男的按id分到b、c三个库。
     * rule(4) val(.person.sex) map(0:dba.person_f, rule->5)
     * rule(5) mod(.person.id, 3) map(0:dbb.person_m, 1:dbc.person_m)
     */
    @Test
    public void test4() {
        RuleParser ruleParser = new RuleParser();
        RulesSet rulesSet = ruleParser.parse(
                "rule(4) val(.person.sex)   map(0:dba.person_f, rule->5)\n" +
                "rule(5) mod(.person.id, 3) map(0:dbb.person_m, 1:dbc.person_m)");

        RealPartition realPartition = rulesSet.find(new MatrixTableFieldValue("person", "sex", "0"));
        assertThat(realPartition.databaseName, is("dba"));
        assertThat(realPartition.tableName, is("person_f"));

        realPartition = rulesSet.find(
                new MatrixTableFieldValue("person", "sex", "1"),
                new MatrixTableFieldValue("person", "id", "0"));
        assertThat(realPartition.databaseName, is("dbb"));
        assertThat(realPartition.tableName, is("person_m"));
    }

    /**
     * 5. 按范围，映射数据库。
     * rule(6) range(.order.id, ~1000:A, 1000~2000:B, 2000~:C), map(db$)
     */
    @Test
    public void test6() {
        RuleParser ruleParser = new RuleParser();
        RulesSet rulesSet = ruleParser.parse("rule(6) range(.order.id, ~1000:A, 1000~2000:B, 2000~:C) map(db$)");

        RealPartition realPartition = rulesSet.find(
                new MatrixTableFieldValue("person", "sex", "0"),
                new MatrixTableFieldValue("order", "id", "1001"));
        assertThat(realPartition.databaseName, is("dbB"));
        assertThat(realPartition.tableName, is(""));

        realPartition = rulesSet.find(
                new MatrixTableFieldValue("person", "sex", "1"),
                new MatrixTableFieldValue("order", "id", "2001"));
        assertThat(realPartition.databaseName, is("dbC"));
        assertThat(realPartition.tableName, is(""));
    }
}
