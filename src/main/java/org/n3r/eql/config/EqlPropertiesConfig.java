package org.n3r.eql.config;

import org.n3r.eql.util.EqlUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

public class EqlPropertiesConfig implements EqlConfig {
    private final Properties properties;

    public EqlPropertiesConfig(String properties) {
        this(EqlUtils.toProperties(properties));
    }

    public EqlPropertiesConfig(File file) {
        this(EqlUtils.toProperties(file));
    }

    public EqlPropertiesConfig(InputStream is) {
        this(EqlUtils.toProperties(is));
    }

    public EqlPropertiesConfig(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getStr(String key) {
        return properties.getProperty(key);
    }

}
