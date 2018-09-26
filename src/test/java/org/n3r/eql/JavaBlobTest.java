package org.n3r.eql;

import com.google.common.base.Charsets;
import lombok.Data;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class JavaBlobTest {
    @BeforeClass
    public static void beforeClass() {
        Eqll.choose("orcl");
    }

    @Test
    public void testOracleBlob() {
        val china = "中华人民共和国";

        new Eqll().id("insertBlob").params(china).execute();
        byte[] bytes = new Eqll().id("selectBlob").limit(1).execute();
        assertThat(new String(bytes, Charsets.UTF_8)).isEqualTo(china);

        String ret = new Eqll().id("selectBlobString").limit(1).execute();
        assertThat(ret).isEqualTo(china);

        AsResult asResult = new Eqll().id("selectBlobAsResult").limit(1).execute();
        assertThat(asResult.getSeq()).isEqualTo(1);
        assertThat(asResult.getRemark()).isEqualTo(china);

        String taiwan = "台湾省";
        Integer effectedRows = new Eqll().id("updateBlob").params(taiwan).execute();
        assertThat(effectedRows).isEqualTo(1);
        ret = new Eqll().id("selectBlobString").limit(1).execute();
        assertThat(ret).isEqualTo(taiwan);
    }

    @Data
    public static class AsResult {
        private String state;
        private String remark;
        private int seq;
    }
}
