package org.n3r.eql.config;

public class EqlJdbcConfig implements EqlConfig {
    private String driver, url, user, password;

    public EqlJdbcConfig() {
    }

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

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
