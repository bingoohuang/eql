package org.n3r.eql.liulei;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.n3r.eql.base.EqlToProperties;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


@Data @NoArgsConstructor @AllArgsConstructor
public class MemberCard implements EqlToProperties {
    String mbrCardId;
    Timestamp startTime, endTime;
    int times;
    Timestamp updateTime;
    int availTimes;
    Timestamp createTime;

    @Override public Map<String, Object> toProperties() {
        HashMap<String, Object> props = new HashMap<String, Object>(3);
        props.put("mbrCardId", mbrCardId);
        props.put("startTime", startTime);
        props.put("endTime", endTime);

        return props;
    }
}
