package org.n3r.eql.parser;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.n3r.eql.base.DynamicLanguageDriver;
import org.n3r.eql.base.EqlResourceLoader;
import org.n3r.eql.impl.FileEqlResourceLoader;
import org.n3r.eql.map.EqlRun;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EqlParserTest {
    @Test
    public void includeSqlId() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [commondCondition]\n" +
                "A == #a#\n" +
                "-- [lookup]\n" +
                "select 1\n" +
                "-- include commondCondition");

        EqlBlock eqlBlock = map.get("lookup");
        assertThat(((StaticSql) eqlBlock.getSqls().get(0)).getSql(), is("select 1\nA == #a#\n"));
    }

    @Test
    public void oneBlock1IfIf() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [selectIf2 returnType=org.n3r.eql.SimpleTest$Bean]\n" +
                "SELECT A,B,C,D,E\n" +
                "FROM ESQL_TEST\n" +
                "WHERE A = #a#\n" +
                "AND\n" +
                "-- if e == 100\n" +
                "C = #c#\n" +
                "/* if a == 1 */ AND B = 'A' /* end */\n" +
                "and 1 = 1\n" +
                "-- elseif e == 200\n" +
                "C = 'AC'\n" +
                "-- else\n" +
                "C = 'FALSE'\n" +
                "-- end");

        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());
        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("selectIf2"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        assertThat(dynamicSql.getParts().size(), is(2));
        EqlPart eqlPart = dynamicSql.getParts().part(0);
        Map<String, Object> context = Maps.newHashMap();
        EqlRun eqlRun = new EqlRun();
        eqlRun.setExecutionContext(context);
        assertThat(eqlPart.evalSql(eqlRun), is("SELECT A,B,C,D,E\nFROM ESQL_TEST\nWHERE A = #a#\nAND\n"));

        IfPart ifPart = (IfPart) dynamicSql.getParts().part(1);
        System.out.println(ifPart);
    }

    @Test
    public void oneBlock1StaticSql() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG");

        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());
        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof StaticSql, is(true));

        StaticSql staticSql = (StaticSql) sqls.get(0);
        assertThat(staticSql.getSql(), is("SELECT * FROM BLOG\n"));
    }

    @Test
    public void oneBlock2StaticSqls() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG1;\r\n"
                + "SELECT * FROM BLOG2\r\n");

        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());
        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(2));
        assertThat(sqls.get(0) instanceof StaticSql, is(true));
        assertThat(sqls.get(1) instanceof StaticSql, is(true));

        StaticSql staticSql1 = (StaticSql) sqls.get(0);
        assertThat(staticSql1.getSql(), is("SELECT * FROM BLOG1\n"));
        StaticSql staticSql2 = (StaticSql) sqls.get(1);
        assertThat(staticSql2.getSql(), is("SELECT * FROM BLOG2\n"));
    }

    @Test
    public void twoBlock1StaticSql() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog1]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- [queryBlog2]\r\n"
                + "SELECT * FROM BLOG"
        );

        EqlBlock eqlBlock = map.get("queryBlog1");
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof StaticSql, is(true));

        StaticSql staticSql = (StaticSql) sqls.get(0);
        assertThat(staticSql.getSql(), is("SELECT * FROM BLOG\n"));

        eqlBlock = map.get("queryBlog2");
        assertThat(eqlBlock.getSqlId(), is("queryBlog2"));
        sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof StaticSql, is(true));

        staticSql = (StaticSql) sqls.get(0);
        assertThat(staticSql.getSql(), is("SELECT * FROM BLOG\n"));
    }

    @Test
    public void oneBlock1DynamicIfSql() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- if name != null\r\n"
                + "   where name = #name#\r\n"
                + "-- end"
        );

        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());

        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof IfPart, is(true));
        IfPart ifPart = (IfPart) eqlPart1;
        assertThat(ifPart.getConditions().size(), is(1));

        IfCondition condition = ifPart.getConditions().get(0);
        assertThat(condition.getExpr(), is("name != null"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#\n"));
    }


    @Test
    public void oneBlock1DynamicSwitchSql() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- switch name\r\n"
                + "--    case bingoo\r\n"
                + "        where name = #name#\r\n"
                + "-- end"
        );

        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());
        checkOneBlock1DynamicSwitchSql(blocks);
    }

    @Test
    public void oneBlock1DynamicSwitchSqlInline() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* switch name */"
                + "/*    case bingoo */"
                + "        where name = #name#"
                + "/* end */"
        );

        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());
        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof SwitchPart, is(true));
        SwitchPart part = (SwitchPart) eqlPart1;
        assertThat(part.getCases().size(), is(1));

        IfCondition condition = part.getCases().get(0);
        assertThat(condition.getExpr(), is("bingoo"));
        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#"));
    }

    private void checkOneBlock1DynamicSwitchSql(List<EqlBlock> blocks) {
        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof SwitchPart, is(true));
        SwitchPart part = (SwitchPart) eqlPart1;
        assertThat(part.getCases().size(), is(1));

        IfCondition condition = part.getCases().get(0);
        assertThat(condition.getExpr(), is("bingoo"));
        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#\n"));
    }

    @Test
    public void oneBlock1DynamicSwitchSql2() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- switch name\r\n"
                + "-- case bingoo\r\n"
                + "     where name = #name#\r\n"
                + "-- default\r\n"
                + "     where sex = #sex#\r\n"
                + "-- end"
        );

        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());

        checkOneBlock1DynamicSwitchSql2(blocks);
    }

    @Test
    public void oneBlock1DynamicSwitchSql2Inlie() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* switch name*/\n"
                + "-- case bingoo\r\n"
                + "     where name = #name#\r\n"
                + "-- default\r\n"
                + "     where sex = #sex#\r\n"
                + "/* end */"
        );
        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());

        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof SwitchPart, is(true));
        SwitchPart part = (SwitchPart) eqlPart1;
        assertThat(part.getCases().size(), is(2));

        IfCondition condition = part.getCases().get(0);
        assertThat(condition.getExpr(), is("bingoo"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#\n"));

        condition = part.getCases().get(1);
        assertThat(condition.getExpr(), is(""));
        value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where sex = #sex#\n"));
    }

    private void checkOneBlock1DynamicSwitchSql2(List<EqlBlock> blocks) {
        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof SwitchPart, is(true));
        SwitchPart part = (SwitchPart) eqlPart1;
        assertThat(part.getCases().size(), is(2));

        IfCondition condition = part.getCases().get(0);
        assertThat(condition.getExpr(), is("bingoo"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#\n"));

        condition = part.getCases().get(1);
        assertThat(condition.getExpr(), is(""));
        value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where sex = #sex#\n"));
    }


    @Test
    public void oneBlock1DynamicIfSql2() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* if name != null */ where name = #name# /* end */"
        );

        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());
        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof IfPart, is(true));
        IfPart ifPart = (IfPart) eqlPart1;
        assertThat(ifPart.getConditions().size(), is(1));

        IfCondition condition = ifPart.getConditions().get(0);
        assertThat(condition.getExpr(), is("name != null"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#"));
    }

    @Test
    public void oneBlock1DynamicIfSql3() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* if name != null */ \r\n where name = #name# \r\n/* end */"
        );
        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());

        checkBlock1DynamicIfSql(blocks);
    }

    private void checkBlock1DynamicIfSql(List<EqlBlock> blocks) {
        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof IfPart, is(true));
        IfPart ifPart = (IfPart) eqlPart1;
        assertThat(ifPart.getConditions().size(), is(1));

        IfCondition condition = ifPart.getConditions().get(0);
        assertThat(condition.getExpr(), is("name != null"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#\n"));
    }

    @Test
    public void oneBlock1DynamicIfElseIfSql() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- if name != null\r\n"
                + "   where name = #name#\r\n"
                + "-- else if sex != null\r\n"
                + "   where sex = #sex#\r\n"
                + "-- end"
        );

        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());
        checkOneBlock1DynamicIfElseIfSql(blocks);
    }

    @Test
    public void oneBlock1DynamicIfElseIfSql2() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "/* if name != null*/"
                + "   where name = #name#\r\n"
                + "/* else if sex != null*/"
                + "   where sex = #sex#\r\n"
                + "-- end"
        );
        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());

        checkOneBlock1DynamicIfElseIfSql(blocks);
    }

    private void checkOneBlock1DynamicIfElseIfSql(List<EqlBlock> blocks) {
        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof IfPart, is(true));
        IfPart ifPart = (IfPart) eqlPart1;
        assertThat(ifPart.getConditions().size(), is(2));

        IfCondition condition = ifPart.getConditions().get(0);
        assertThat(condition.getExpr(), is("name != null"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#\n"));

        condition = ifPart.getConditions().get(1);
        assertThat(condition.getExpr(), is("sex != null"));
        value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where sex = #sex#\n"));
    }

    @Test
    public void oneBlock1DynamicIfElseSql() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG\r\n"
                + "-- if name != null\r\n"
                + "   where name = #name#\r\n"
                + "-- else\r\n"
                + "   where sex = #sex#\r\n"
                + "-- end"
        );
        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());

        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof IfPart, is(true));
        IfPart ifPart = (IfPart) eqlPart1;
        assertThat(ifPart.getConditions().size(), is(2));

        IfCondition condition = ifPart.getConditions().get(0);
        assertThat(condition.getExpr(), is("name != null"));

        MultiPart value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where name = #name#\n"));


        condition = ifPart.getConditions().get(1);
        assertThat(condition.getExpr(), is("true"));
        value = condition.getValue();
        assertThat(((LiteralPart) value.part(0)).getSql(), is("where sex = #sex#\n"));
    }

    @Test
    public void oneBlock1DynamicForSql() {
        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(
                "-- [queryBlog]\r\n"
                + "SELECT * FROM BLOG where name in\r\n"
                + "-- for item=item collection=names open=( close=) seperate=,\r\n"
                + "   #item#\r\n"
                + "-- end"
        );
        List<EqlBlock> blocks = new ArrayList<EqlBlock>(map.values());

        assertThat(blocks.size(), is(1));

        EqlBlock eqlBlock = blocks.get(0);
        assertThat(eqlBlock.getSqlId(), is("queryBlog"));
        List<Sql> sqls = eqlBlock.getSqls();
        assertThat(sqls.size(), is(1));
        assertThat(sqls.get(0) instanceof DynamicSql, is(true));

        DynamicSql dynamicSql = (DynamicSql) sqls.get(0);
        MultiPart sqlParts = dynamicSql.getParts();
        assertThat(sqlParts.size(), is(2));

        EqlPart eqlPart0 = sqlParts.part(0);
        assertThat(eqlPart0 instanceof LiteralPart, is(true));
        assertThat(((LiteralPart) eqlPart0).getSql(), is("SELECT * FROM BLOG where name in\n"));
        EqlPart eqlPart1 = sqlParts.part(1);
        assertThat(eqlPart1 instanceof ForPart, is(true));
        ForPart part = (ForPart) eqlPart1;
        assertThat(((LiteralPart)part.getSqlPart().part(0)).getSql(), is("#item#\n"));
    }

    @Test(expected=Exception.class)
    public void duplicatedSqlIdImportBlock() {
        EqlParser eqlParser = new EqlParser(null, "");
        eqlParser.parse(
                "-- [selectIf2 returnType=org.n3r.eql.SimpleTest$Bean]\n" +
                        "SELECT A,B,C,D,E\n" +
                        "FROM ESQL_TEST\n" +
                        "-- import org/n3r/eql/DynamicTest.eql\r\n"
        );
    }
}
