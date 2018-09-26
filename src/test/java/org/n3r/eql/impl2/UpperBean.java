package org.n3r.eql.impl2;

import lombok.Data;
import org.n3r.eql.convert.*;

@Data
public class UpperBean {
    @Upper
    private String name;
    @Lower
    private String addr;
    @DayString(format = "yyyy-MM-dd")
    private String day;
    @yyyyMMdd
    private String day2;
    @HHmmss
    private String time;
}
