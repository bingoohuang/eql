package org.n3r.eql.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.val;
import org.junit.Test;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class OgTest {
    @Test
    public void test() {
        val json  = "{\"_date\":1517405139224,\"selectedId\":\"condition.getSelectedId()\",\"_lastResult\":\"\",\"_ip\":\"10.10.10.103\",\"_paramsCount\":1,\"_dynamicsCount\":1,\"_host\":\"bogon\",\"_results\":[],\"_1\":{\"selectedId\":\"condition.getSelectedId()\",\"startTime\":\"startTime\",\"endTime\":\"endTime\",\"tollFlag\":\"1\",\"table\":\"glass_vod_statistics_program_flow\"},\"_dynamics\":[{\"table\":\"glass_vod_statistics_program_flow\"}],\"startTime\":\"startTime\",\"endTime\":\"endTime\",\"_time\":1517405139224,\"tollFlag\":\"1\",\"table\":\"glass_vod_statistics_program_flow\",\"_params\":[{\"$ref\":\"$._1\"}]}";

        Map<String, Object> map = JSON.parseObject(json, new TypeReference<Map<String, Object>>() {
        });

        Object eval = Og.eval("table == \"glass_vod_statistics_program_flow\"", map, null);
        assertThat(eval).isEqualTo(Boolean.TRUE);
    }
}
