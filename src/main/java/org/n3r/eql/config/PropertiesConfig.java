package org.n3r.eql.config;

import java.util.Properties;

public class PropertiesConfig implements EqlConfig {
    private final Properties properties;

    public PropertiesConfig(Properties properties) {
        this.properties =  properties;
    }

    @Override
    public String getStr(String key) {
        return properties.getProperty(key);
    }
}
