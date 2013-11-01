package org.n3r.eql;

import com.google.common.base.Charsets;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaBlobTest {
    @BeforeClass
    public static void beforeClass() {
        Eqll.choose("orcl");
    }

    @Test
    @Ignore
    public void testOracleBlob() {
        new Eqll().id("insertBlob").params("中华人民共和国").execute();
        byte[] bytes = new Eqll().id("selectBlob").limit(1).execute();
        assertThat(new String(bytes, Charsets.UTF_8), is("中华人民共和国"));

        String ret = new Eqll().id("selectBlobString").limit(1).execute();
        assertThat(ret, is("中华人民共和国"));

        AsResult asResult = new Eqll().id("selectBlobAsResult").limit(1).execute();
        assertThat(asResult.getSeq(), is(1));
        assertThat(asResult.getRemark(), is("中华人民共和国"));

        Integer effectedRows = new Eqll().id("updateBlob").params("台湾省").execute();
        assertThat(effectedRows, is(1));
        ret = new Eqll().id("selectBlobString").limit(1).execute();
        assertThat(ret, is("台湾省"));
    }

    public static class AsResult {
        private String state;
        private String remark;
        private int seq;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

    }
}
