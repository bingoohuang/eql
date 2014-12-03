package org.n3r.eql.config;

import org.n3r.eql.util.P;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EqlPropertiesConfig implements EqlConfig {
    private final Properties properties;

    public EqlPropertiesConfig(String properties) {
        this(P.toProperties(properties));
    }

    public EqlPropertiesConfig(File file) {
        this(P.toProperties(file));
    }

    public EqlPropertiesConfig(InputStream is) {
        this(P.toProperties(is));
    }

    public EqlPropertiesConfig(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getStr(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Map<String, String> params() {
        HashMap<String, String> map = new HashMap<String, String>(properties.size());
        for (final String name : properties.stringPropertyNames())
            map.put(name, properties.getProperty(name));
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqlPropertiesConfig that = (EqlPropertiesConfig) o;

        if (!properties.equals(that.properties)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }
}
