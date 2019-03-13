package org.n3r.eql.parser;

import lombok.val;
import org.junit.Test;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class FengyuCaseTest {
    @Test
    public void test1() {
        val sql = "-- [xxx]\n" +
                "select *\n" +
                "   from merchant m \n" +
                "       ,merchant_saas ms  \n" +
                "       ,merchant_shop msp  \n" +
                "  where m.id = ms.merchant_id \n" +
                "    and m.id = msp.merchant_id\n" +
                "     /* switch _1 */\n" +
                "\n" +
                "     /* case 3 */\n" +
                "    and state_stream = '已订购'\n" +
                "     /* isNotEmpty _6 */\n" +
                "        and ms.serv_end_time >= #_6#\n" +
                "     /* end */\n" +
                "     /* isNotEmpty _6 */\n" +
                "        and ms.serv_end_time <= #_7#\n" +
                "     /* end */\n" +
                "\n" +
                "     /* end */";

        EqlParser eqlParser = new EqlParser(null, "");
        Map<String, EqlBlock> map = eqlParser.parse(sql);

        val dynamicSql = (DynamicSql)map.get("xxx").sqls.get(0);
        val switchPart = (SwitchPart)dynamicSql.getParts().part(1);
        IfCondition actual = switchPart.getCases().get(0);
        assertThat(actual.getValue().size()).isEqualTo(3);
    }
}
