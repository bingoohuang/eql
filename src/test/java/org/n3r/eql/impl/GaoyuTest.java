package org.n3r.eql.impl;

import lombok.val;
import org.junit.Test;
import org.n3r.eql.Eql;
import org.n3r.eql.map.EqlRun;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class GaoyuTest {

    @Test
    public void testFromGaoyu() {
        val tableName = "glass_vod_statistics_program_flow";
        Eql eql = new Eql().select("getVisitDetailInfo").params(of("startTime", "startTime", "endTime", "endTime",
                "selectedId", "condition.getSelectedId()", "table", tableName, "tollFlag", "1"))
                .dynamics(of("table", tableName));

        List<EqlRun> eqlRuns = eql.evaluate();
        assertThat(eqlRuns.get(0).getRunSql()).contains("IF(LICENSE_TYPE");
    }

    public Map<String, Object> of(String... kvs) {
        val map = new HashMap<String, Object>();

        for (int i = 0, ii = kvs.length; i + 1 < ii; i += 2) {
            map.put(kvs[i], kvs[i + 1]);
        }

        return map;
    }
}
