package org.n3r.eql.spec;

import java.util.ArrayList;
import java.util.List;

public class Spec {
    private String name;
    private List<String> params = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String[] getParams() {
        return params.toArray(new String[0]);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addParam(String param) {
        params.add(param);
    }
}
