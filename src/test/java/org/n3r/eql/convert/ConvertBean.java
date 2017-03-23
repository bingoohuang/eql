package org.n3r.eql.convert;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.n3r.eql.convert.todb.ToDbDecode;
import org.n3r.eql.convert.todb.ToDbTimestamp;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/6.
 */
@Data @AllArgsConstructor @NoArgsConstructor
public class ConvertBean {
    private String id;
    @TruncateTail(".00")
    private String times;
    private String times2;
    @TruncateTail(".00")
    private String times3;

    @DayString(format = "yyyy-MM-dd")
    @ToDbTimestamp(format = "yyyy-MM-dd")
    private String updateTime;

    @EqlDecode(value = {"M", "true", "false"}, toType = "boolean")
    @ToDbDecode({"true", "M", "F"})
    private boolean sex;

    @JSONField(serialize = false)
    public String getXxx() {
        throw new RuntimeException("Should not called");
    }

    @JSONField(serialize = false)
    public String isYyy() {
        throw new RuntimeException("Should not called");
    }
}
