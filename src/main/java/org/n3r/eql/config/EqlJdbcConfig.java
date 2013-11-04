package org.n3r.eql.config;

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
        if ("driver".equals(key)) return driver;
        if ("url".equals(key)) return url;
        if ("user".equals(key)) return user;
        if ("password".equals(key)) return password;

        return "";
    }
}
