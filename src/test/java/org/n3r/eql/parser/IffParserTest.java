package org.n3r.eql.parser;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IffParserTest {
    @Test
    public void test() {
        EqlParser eqlParser = new EqlParser();
        Map<String, EqlBlock> map = eqlParser.parse("",
                "-- [lookup]\n" +
                        "select 1\n" +
                        "-- iff a == 1\n" +
                        " xxx\n" +
                        "-- iff b == 1\n" +
                        "yyy"
        );

        EqlBlock eqlBlock = map.get("lookup");
        DynamicSql dynamicSql = (DynamicSql) eqlBlock.getSqls().get(0);
        assertThat(dynamicSql.getParts().size(), is(3));
        assertThat(((LiteralPart)dynamicSql.getParts().part(0)).getSql(), is("select 1\n"));
        assertThat(((IffPart)dynamicSql.getParts().part(1)).getExpr(), is("a == 1"));
        assertThat(((IffPart)dynamicSql.getParts().part(1)).getPart().getSql(), is("xxx\n"));
        assertThat(((IffPart)dynamicSql.getParts().part(2)).getExpr(), is("b == 1"));
        assertThat(((IffPart)dynamicSql.getParts().part(2)).getPart().getSql(), is("yyy\n"));
    }
}
