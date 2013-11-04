package org.n3r.eql.config;

import java.util.Properties;

public class EqlPropertiesConfig implements EqlConfig {
    private final Properties properties;


    public EqlPropertiesConfig(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getStr(String key) {
        return properties.getProperty(key);
    }

}
