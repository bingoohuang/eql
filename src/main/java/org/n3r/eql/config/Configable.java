package org.n3r.eql.config;

import java.util.Properties;

public interface Configable {
    boolean exists(String key);

    Properties getProperties();

    String getStr(String key);

    String getStr(String key, String defaultValue);
}
