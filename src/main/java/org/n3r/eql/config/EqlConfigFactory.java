package org.n3r.eql.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EqlConfigFactory {
    public static InputStream classResourceToInputStream(String pathname, boolean silent) {
        InputStream is = getClassResourceAsStream(pathname);
        if (is != null || silent) return is;

        throw new RuntimeException("fail to find " + pathname + " in current dir or classpath");
    }

    public static InputStream getClassResourceAsStream(String resourceName) {
        return EqlConfigFactory.class.getClassLoader().getResourceAsStream(resourceName);
    }

    public static EqlConfig parseConfig(String key) {
        String configFile = "eql/eql-" + key + ".properties";
        InputStream inputStream = classResourceToInputStream(configFile, true);
        if (inputStream != null) return parseConfig(inputStream);

        throw new RuntimeException("no " + configFile + " found on the classpath");
    }

    public static EqlConfig parseConfig(InputStream is) {
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("parse config error", e);
        }
        return new PropertiesConfig(properties);
    }
}
