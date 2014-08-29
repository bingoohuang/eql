package org.n3r.eql.util;

import com.google.common.base.Charsets;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlPropertiesConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EqlPropertiesConfigFactory {
    public static EqlConfig parseEqlProperties(String key) {
        String configFile = "eql/eql-" + key + ".properties";
        InputStream inputStream = C.classResourceToInputStream(configFile, true);
        if (inputStream != null) return parseConfig(inputStream);

        throw new RuntimeException("no " + configFile + " found on the classpath");
    }


    public static EqlConfig parseConfig(String str) {
        return parseConfig(new ByteArrayInputStream(str.getBytes(Charsets.UTF_8)));
    }

    public static EqlConfig parseConfig(InputStream is) {
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("parse config error", e);
        }
        return new EqlPropertiesConfig(properties);
    }
}
