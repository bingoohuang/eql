package org.n3r.eql.impl;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.n3r.eql.Eql;
import org.n3r.eql.codedesc.CodeDescMapper;
import org.n3r.eql.map.EqlRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CodeDescTest {
    @Before
    public void beforeClass() {
        new Eql("mysql").execute();
    }

    @Test
    public void test1() {
        List<CodeDesc> codeDescs = new Eql("mysql").returnType(CodeDesc.class).execute();
        assertThat(codeDescs.size(), is(4));

        assertThat(codeDescs.get(0), is(equalTo(new CodeDesc(1L, "处理中", "进行中"))));
        assertThat(codeDescs.get(1), is(equalTo(new CodeDesc(2L, "处理完毕", "已完成"))));
        assertThat(codeDescs.get(2), is(equalTo(new CodeDesc(3L, "处理失败", "已失败"))));
        assertThat(codeDescs.get(3), is(equalTo(new CodeDesc(4L, "未知", "未识别"))));
    }

    @Test
    public void test2() {
        MockDiamondServer.setUpMockServer();

        List<CodeDesc> codeDescs = new Eql("mysql").returnType(CodeDesc.class).execute();
        assertThat(codeDescs.size(), is(4));

        assertThat(codeDescs.get(0), is(equalTo(new CodeDesc(1L, "AA处理中", "进行中"))));
        assertThat(codeDescs.get(1), is(equalTo(new CodeDesc(2L, "BB处理完毕", "已完成"))));
        assertThat(codeDescs.get(2), is(equalTo(new CodeDesc(3L, "CC处理失败", "已失败"))));
        assertThat(codeDescs.get(3), is(equalTo(new CodeDesc(4L, "DD未知", "未识别"))));

        new Eql("mysql").id("updatexx").execute();

        MockDiamondServer.setConfigInfo("EQL.CACHE.DESC", "org.n3r.eql.impl.CodeDescTest.eql", "meaning2.cacheVersion=" + System.currentTimeMillis());
        codeDescs = new Eql("mysql").returnType(CodeDesc.class).execute();
        assertThat(codeDescs.size(), is(4));

        assertThat(codeDescs.get(0), is(equalTo(new CodeDesc(1L, "AA处理中", "进行中xx"))));
        assertThat(codeDescs.get(1), is(equalTo(new CodeDesc(2L, "BB处理完毕", "已完成xx"))));
        assertThat(codeDescs.get(2), is(equalTo(new CodeDesc(3L, "CC处理失败", "已失败xx"))));
        assertThat(codeDescs.get(3), is(equalTo(new CodeDesc(4L, "DD未知", "未识别xx"))));

        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void test3() {
        List<String> states = new Eql("mysql").id("test1").returnType(new EqlRowMapper() {
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString(2);
            }
        }).execute();

        List<String> expect = Lists.newArrayList("处理中", "处理完毕", "处理失败", "未知");
        assertThat(states, is(equalTo(expect)));
    }

    public static class CodeDesc {
        long code;
        String name;
        String meaning;

        public CodeDesc() {
        }

        public CodeDesc(long code, String name, String meaning) {
            this.code = code;
            this.name = name;
            this.meaning = meaning;
        }

        public long getCode() {
            return code;
        }

        public void setCode(long code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMeaning() {
            return meaning;
        }

        public void setMeaning(String meaning) {
            this.meaning = meaning;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CodeDesc codeDesc = (CodeDesc) o;

            if (code != codeDesc.code) return false;
            if (meaning != null ? !meaning.equals(codeDesc.meaning) : codeDesc.meaning != null) return false;
            if (name != null ? !name.equals(codeDesc.name) : codeDesc.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (code ^ (code >>> 32));
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (meaning != null ? meaning.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "CodeDesc{" +
                    "code=" + code +
                    ", name='" + name + '\'' +
                    ", meaning='" + meaning + '\'' +
                    '}';
        }
    }

    public static class CustomMapping implements CodeDescMapper {
        final Map<String, String> mapping = new HashMap<String, String>() {{
            put("1", "AA处理中");
            put("2", "BB处理完毕");
            put("3", "CC处理失败");
        }};

        String defaultDesc = "DD未知";

        public String map(String code) {
            return mapping.containsKey(code) ? mapping.get(code) : defaultDesc;
        }
    }
}
