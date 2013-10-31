package org.n3r.eql.parser;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SqlParserTest {
    static String sqlClassPath = "";

    @Test
    public void oneBlock1IfIf() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [selectIf2 returnType=org.n3r.eql.SimpleTest$Bean]\n" +
                "SELECT A,B,C,D,E\n" +
                "FROM ESQL_TEST\n" +
                "WHERE A = #a#\n" +
                "AND\n" +
                "-- if e == 100\n" +
                "C = #c#\n" +
                "/* if a == 1 */ AND B = 'A' /* end */\n" +
                "and 1 = 1\n"  +
                "-- elseif e == 200\n" +
                "C = 'AC'\n" +
                "-- else\n" +
                "C = 'FALSE'\n" +
                "-- end");

        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());
        assertThat(blocks.size(), is(1));

        SqlBlock sqlBlock = blocks.get(0);
        assertThat(sqlBlock.getSqlId(), is("selectIf2"));
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        assertThat(dynamicSql.getParts().size(), is(2));
        SqlPart sqlPart = dynamicSql.getParts().part(0);
        assertThat(sqlPart.evalSql(null), is("SELECT A,B,C,D,E\nFROM ESQL_TEST\nWHERE A = #a#\nAND"));

        IfPart ifPart = (IfPart) dynamicSql.getParts().part(1);
        System.out.println(ifPart);
    }

    @Test
    public void oneBlock1StaticSql() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG");

        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());
        assertThat(blocks.size(), is(1));

        SqlBlock sqlBlock = blocks.get(0);
        assertThat(sqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof StaticSql, is(true));

        StaticSql staticSql = (StaticSql) sqls.get(0);
        assertThat(staticSql.getSql(), is("SELECT * FROM BLOG"));
    }

    @Test
    public void oneBlock2StaticSqls() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG1;\r\n"
                + "SELECT * FROM BLOG2\r\n");

        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());
        assertThat(blocks.size(), is(1));

        SqlBlock sqlBlock = blocks.get(0);
        assertThat(sqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(2));
        assertThat(sqls.get(0) instanceof StaticSql, is(true));
        assertThat(sqls.get(1) instanceof StaticSql, is(true));

        StaticSql staticSql1 = (StaticSql) sqls.get(0);
        assertThat(staticSql1.getSql(), is("SELECT * FROM BLOG1"));
        StaticSql staticSql2 = (StaticSql) sqls.get(1);
        assertThat(staticSql2.getSql(), is("SELECT * FROM BLOG2"));
    }

    @Test
    public void twoBlock1StaticSql() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog1]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- [queryBlog2]\r\n"
                + "SELECT * FROM BLOG"
        );

        SqlBlock sqlBlock = map.get("queryBlog1");
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof StaticSql, is(true));

        StaticSql staticSql = (StaticSql) sqls.get(0);
        assertThat(staticSql.getSql(), is("SELECT * FROM BLOG"));

        sqlBlock = map.get("queryBlog2");
        assertThat(sqlBlock.getSqlId(), is("queryBlog2"));
        sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof StaticSql, is(true));

        staticSql = (StaticSql) sqls.get(0);
        assertThat(staticSql.getSql(), is("SELECT * FROM BLOG"));
    }

    @Test
    public void oneBlock1DynamicIfSql() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- if name != null\r\n"
                + "   where name = #name#\r\n"
                + "-- end"
        );

        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());

        checkBlock1DynamicIfSql(blocks);
    }


    @Test
    public void oneBlock1DynamicSwitchSql() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- switch name\r\n"
                + "--    case bingoo\r\n"
                + "        where name = #name#\r\n"
                + "-- end"
        );

        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());
        checkOneBlock1DynamicSwitchSql(blocks);
    }

    @Test
    public void oneBlock1DynamicSwitchSqlInline() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* switch name */"
                + "/*    case bingoo */"
                + "        where name = #name#"
                + "/* end */"
        );

        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());
        checkOneBlock1DynamicSwitchSql(blocks);
    }

    private void checkOneBlock1DynamicSwitchSql(List<SqlBlock> blocks) {
        assertThat(blocks.size(), is(1));

        SqlBlock sqlBlock = blocks.get(0);
        assertThat(sqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        SqlPart sqlPart0 = sqlParts.part(0);
        assertThat(sqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) sqlPart0).getSql(), is("SELECT * FROM BLOG"));
        SqlPart sqlPart1 = sqlParts.part(1);
        assertThat(sqlPart1 instanceof SwitchPart, is(true));
        SwitchPart part = (SwitchPart) sqlPart1;
        assertThat(part.getCases().size(), is(1));

        IfCondition condition = part.getCases().get(0);
        assertThat(condition.getExpr(), is("bingoo"));
        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#"));
    }

    @Test
    public void oneBlock1DynamicSwitchSql2() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- switch name\r\n"
                + "-- case bingoo\r\n"
                + "     where name = #name#\r\n"
                + "-- default\r\n"
                + "     where sex = #sex#\r\n"
                + "-- end"
        );

        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());

        checkOneBlock1DynamicSwitchSql2(blocks);
    }

    @Test
    public void oneBlock1DynamicSwitchSql2Inlie() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* switch name*/"
                + "-- case bingoo\r\n"
                + "     where name = #name#\r\n"
                + "-- default\r\n"
                + "     where sex = #sex#\r\n"
                + "/* end */"
        );
        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());

        checkOneBlock1DynamicSwitchSql2(blocks);
    }

    private void checkOneBlock1DynamicSwitchSql2(List<SqlBlock> blocks) {
        assertThat(blocks.size(), is(1));

        SqlBlock sqlBlock = blocks.get(0);
        assertThat(sqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        SqlPart sqlPart0 = sqlParts.part(0);
        assertThat(sqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) sqlPart0).getSql(), is("SELECT * FROM BLOG"));
        SqlPart sqlPart1 = sqlParts.part(1);
        assertThat(sqlPart1 instanceof SwitchPart, is(true));
        SwitchPart part = (SwitchPart) sqlPart1;
        assertThat(part.getCases().size(), is(2));

        IfCondition condition = part.getCases().get(0);
        assertThat(condition.getExpr(), is("bingoo"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#"));

        condition = part.getCases().get(1);
        assertThat(condition.getExpr(), is(""));
        value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where sex = #sex#"));
    }


    @Test
    public void oneBlock1DynamicIfSql2() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* if name != null */ where name = #name# /* end */"
        );

        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());
        checkBlock1DynamicIfSql(blocks);
    }

    @Test
    public void oneBlock1DynamicIfSql3() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* if name != null */ \r\n where name = #name# \r\n/* end */"
        );
        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());

        checkBlock1DynamicIfSql(blocks);
    }

    private void checkBlock1DynamicIfSql(List<SqlBlock> blocks) {
        assertThat(blocks.size(), is(1));

        SqlBlock sqlBlock = blocks.get(0);
        assertThat(sqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        SqlPart sqlPart0 = sqlParts.part(0);
        assertThat(sqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) sqlPart0).getSql(), is("SELECT * FROM BLOG"));
        SqlPart sqlPart1 = sqlParts.part(1);
        assertThat(sqlPart1 instanceof IfPart, is(true));
        IfPart ifPart = (IfPart) sqlPart1;
        assertThat(ifPart.getConditions().size(), is(1));

        IfCondition condition = ifPart.getConditions().get(0);
        assertThat(condition.getExpr(), is("name != null"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#"));
    }

    @Test
    public void oneBlock1DynamicIfElseIfSql() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- if name != null\r\n"
                + "   where name = #name#\r\n"
                + "-- else if sex != null\r\n"
                + "   where sex = #sex#\r\n"
                + "-- end"
        );

        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());
        checkOneBlock1DynamicIfElseIfSql(blocks);
    }

    @Test
    public void oneBlock1DynamicIfElseIfSql2() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* if name != null*/"
                + "   where name = #name#\r\n"
                + "/* else if sex != null*/"
                + "   where sex = #sex#\r\n"
                + "-- end"
        );
        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());

        checkOneBlock1DynamicIfElseIfSql(blocks);
    }

    private void checkOneBlock1DynamicIfElseIfSql(List<SqlBlock> blocks) {
        assertThat(blocks.size(), is(1));

        SqlBlock sqlBlock = blocks.get(0);
        assertThat(sqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        SqlPart sqlPart0 = sqlParts.part(0);
        assertThat(sqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) sqlPart0).getSql(), is("SELECT * FROM BLOG"));
        SqlPart sqlPart1 = sqlParts.part(1);
        assertThat(sqlPart1 instanceof IfPart, is(true));
        IfPart ifPart = (IfPart) sqlPart1;
        assertThat(ifPart.getConditions().size(), is(2));

        IfCondition condition = ifPart.getConditions().get(0);
        assertThat(condition.getExpr(), is("name != null"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#"));

        condition = ifPart.getConditions().get(1);
        assertThat(condition.getExpr(), is("sex != null"));
        value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where sex = #sex#"));
    }

    @Test
    public void oneBlock1DynamicIfElseSql() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- if name != null\r\n"
                + "   where name = #name#\r\n"
                + "-- else\r\n"
                + "   where sex = #sex#\r\n"
                + "-- end"
        );
        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());

        assertThat(blocks.size(), is(1));

        SqlBlock sqlBlock = blocks.get(0);
        assertThat(sqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        SqlPart sqlPart0 = sqlParts.part(0);
        assertThat(sqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) sqlPart0).getSql(), is("SELECT * FROM BLOG"));
        SqlPart sqlPart1 = sqlParts.part(1);
        assertThat(sqlPart1 instanceof IfPart, is(true));
        IfPart ifPart = (IfPart) sqlPart1;
        assertThat(ifPart.getConditions().size(), is(2));

        IfCondition condition = ifPart.getConditions().get(0);
        assertThat(condition.getExpr(), is("name != null"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#"));


        condition = ifPart.getConditions().get(1);
        assertThat(condition.getExpr(), is("true"));
        value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where sex = #sex#"));
    }

    @Test
    public void oneBlock1DynamicForSql() {
        SqlParser sqlParser = new SqlParser();
        Map<String, SqlBlock> map = sqlParser.parse(
                sqlClassPath, "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG where name in\r\n"
                + "-- for item=item collection=names open=( close=) seperate=,\r\n"
                + "   #item#\r\n"
                + "-- end"
        );
        List<SqlBlock> blocks = new ArrayList<SqlBlock>(map.values());

        assertThat(blocks.size(), is(1));

        SqlBlock sqlBlock = blocks.get(0);
        assertThat(sqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = sqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        SqlPart sqlPart0 = sqlParts.part(0);
        assertThat(sqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) sqlPart0).getSql(), is("SELECT * FROM BLOG where name in"));
        SqlPart sqlPart1 = sqlParts.part(1);
        assertThat(sqlPart1 instanceof ForPart, is(true));
        ForPart part = (ForPart) sqlPart1;
        assertThat(part.getSqlPart().getSql(), is("#item#"));

    }
}
