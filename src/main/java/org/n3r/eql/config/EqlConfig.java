package org.n3r.eql.config;

import java.util.Map;

public interface EqlConfig {
    String getStr(String key);

    Map<String, String> params();
}
