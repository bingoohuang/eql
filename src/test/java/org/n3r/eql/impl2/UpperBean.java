package org.n3r.eql.impl2;

import org.n3r.eql.convert.*;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public String getDay() {
        return day;
    }

    public String getDay2() {
        return day2;
    }

    public String getTime() {
        return time;
    }
}
