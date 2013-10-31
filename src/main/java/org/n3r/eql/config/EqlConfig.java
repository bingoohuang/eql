package org.n3r.eql.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EqlConfig {
    public static InputStream classResourceToInputStream(String pathname, boolean silent) {
        InputStream is = getClassPathResourceAsStream(pathname);
        if (is != null || silent) return is;

        throw new RuntimeException("fail to find " + pathname + " in current dir or classpath");
    }

    public static InputStream getClassPathResourceAsStream(String resourceName) {
        return EqlConfig.class.getClassLoader().getResourceAsStream(resourceName);
    }

    public static Configable parseConfig(String key) {
        String configFile = "eql/eql-" + key + ".properties";
        InputStream esqlIs = classResourceToInputStream(configFile, true);
        if (esqlIs != null) {
            Properties eslProperties = new Properties();
            try {
                eslProperties.load(esqlIs);
            } catch (IOException e) {
                throw new RuntimeException("load " + configFile + " error", e);
            }
            return new PropertiesConfig(eslProperties);
        }

        throw new RuntimeException("no " + configFile + " found on the classpath");
    }
}
