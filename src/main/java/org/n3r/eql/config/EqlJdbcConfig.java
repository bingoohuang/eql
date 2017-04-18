package org.n3r.eql.config;

import com.google.common.collect.ImmutableMap;
import lombok.Value;

import java.util.Map;

@Value
public class EqlJdbcConfig implements EqlConfig {
    private String driver, url, user, password;

    @Override
    public String getStr(String key) {
        if (EqlConfigKeys.DRIVER.equals(key)) return driver;
        if (EqlConfigKeys.URL.equals(key)) return url;
        if (EqlConfigKeys.USER.equals(key)) return user;
        if (EqlConfigKeys.PASSWORD.equals(key)) return password;

        return "";
    }

    @Override
    public Map<String, String> params() {
        return ImmutableMap.of("driver", driver, "url", url, "user", user, "password", password);
    }
}
