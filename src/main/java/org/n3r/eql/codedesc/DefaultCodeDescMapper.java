package org.n3r.eql.codedesc;

import java.util.HashMap;
import java.util.Map;

public class DefaultCodeDescMapper implements CodeDescMapper {
    Map<String, String> map = new HashMap<String, String>();
    String defaultDesc;


    @Override
    public String map(String code) {
        String desc = map.get(code);
        return desc == null ? defaultDesc : desc;
    }

    public void addMapping(String key, String code) {
        if ("_".equals(key)) defaultDesc = code;
        else map.put(key, code);
    }
}
