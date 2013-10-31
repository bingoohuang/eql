package org.n3r.eql.config;

import java.util.Properties;

public class PropertiesConfig implements Configable {
    private final Properties properties;

    public PropertiesConfig(Properties properties) {
        this.properties =  properties;
    }

    @Override
    public boolean exists(String key) {
        return properties.containsKey(key);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getStr(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getStr(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
