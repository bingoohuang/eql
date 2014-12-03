package org.n3r.eql.config;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class EqlJdbcConfig implements EqlConfig {
    private String driver, url, user, password;

    public EqlJdbcConfig(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqlJdbcConfig that = (EqlJdbcConfig) o;

        if (!driver.equals(that.driver)) return false;
        if (!password.equals(that.password)) return false;
        if (!url.equals(that.url)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = driver.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }
}
