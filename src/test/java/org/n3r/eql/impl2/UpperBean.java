package org.n3r.eql.impl2;

import org.n3r.eql.convert.DayString;
import org.n3r.eql.convert.Lower;
import org.n3r.eql.convert.Upper;

public class UpperBean {
    @Upper
    private String name;
    @Lower
    private String addr;
    @DayString(format = "yyyy-MM-dd")
    private String day;

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
}
