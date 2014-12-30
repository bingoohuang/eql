package org.n3r.eql.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.util.C;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ZhoujunSpOutTruncTest {
    private static String str;

    @BeforeClass
    public static void beforeClass() {
        str = C.classResourceToString("org/n3r/eql/impl/numbers.txt");
        new Eql("jndi").dynamics(str).execute();
    }

    // http://dbasolved.com/2013/06/26/change-varchar2-to-32k-12c-edition/
    // 普通JDBC连接存储过程返回varchar2最大4000字符，使用weblogic数据源，则无此限制
    // Oracle has made a few changes to the database to allow organizations to reduce the cost of migrating to Oracle 12c.
    // One of these changes is with the size limits that have been placed on the VARCHAR2, NVARCHAR2 and RAW data types.
    // In past versions of Oracle database the maximum size for these data types were 4,000 bytes.
    // In Oracle 12c, these data types can now  be increased to 32,767 bytes.
    @Test
    public void test() {
        String sql = "{call $$(#P_HEAD:OUT#,#P_SQL:OUT#,#P_RETCODE:OUT#,#P_RETMSG:OUT#)}";
        Map<String, String> map = new Eql("jndi").dynamics("SP_RPT_ANALYSIS_043_1").execute(sql);
        assertThat(map.get("P_SQL"), equalTo(str));
    }
}
