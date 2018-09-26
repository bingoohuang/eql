package org.n3r.eql.trans;


import com.alibaba.druid.util.JdbcUtils;
import lombok.*;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
@NoArgsConstructor
public class UnpooledDataSource implements DataSource {
    @Getter @Setter private Properties driverProperties;
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<String, Driver>();

    @Getter @Setter private String driverClass;
    @Getter @Setter private String url;
    @Getter @Setter private String username;
    @Getter @Setter private String password;

    @Getter @Setter private Boolean autoCommit;
    @Getter @Setter private Integer defaultTransactionIsolationLevel;

    static {
        val drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            val driver = drivers.nextElement();
            registeredDrivers.put(driver.getClass().getName(), driver);
        }
    }

    public UnpooledDataSource(String driverClass, String url, String username, String password) {
        this.driverClass = driverClass;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(String driverClass, String url, Properties driverProperties) {
        this.driverClass = driverClass;
        this.url = url;
        this.driverProperties = driverProperties;
    }

    public void setDriver(String driver) {
        this.driverClass = driver;
    }

    public String getDriver() {
        return this.driverClass;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.url = jdbcUrl;
    }

    public String getJdbcUrl() {
        return this.url;
    }

    public void setUser(String user) {
        this.username = user;
    }

    public String getUser() {
        return this.username;
    }

    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    public void setLoginTimeout(int loginTimeout) {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    public void setLogWriter(PrintWriter logWriter) {
        DriverManager.setLogWriter(logWriter);
    }

    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    private Connection doGetConnection(String username, String password) throws SQLException {
        val props = new Properties();
        if (driverProperties != null) {
            props.putAll(driverProperties);
        }
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        return doGetConnection(props);
    }

    private Connection doGetConnection(Properties properties) throws SQLException {
        initializeDriver();
        val connection = DriverManager.getConnection(url, properties);
        configureConnection(connection);
        return connection;
    }

    private synchronized void initializeDriver() throws SQLException {
        if (this.driverClass == null || this.driverClass.isEmpty()) {
            this.driverClass = JdbcUtils.getDriverClassName(url);
        }

        if (!registeredDrivers.containsKey(driverClass)) {
            Class<?> driverType;
            try {
                driverType = Class.forName(driverClass);
                // DriverManager requires the driver to be loaded via the system ClassLoader.
                // http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
                Driver driverInstance = (Driver) driverType.newInstance();
                DriverManager.registerDriver(new DriverProxy(driverInstance));
                registeredDrivers.put(driverClass, driverInstance);
            } catch (Exception e) {
                throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
            }
        }
    }

    private void configureConnection(Connection conn) throws SQLException {
        if (autoCommit != null && autoCommit != conn.getAutoCommit()) {
            conn.setAutoCommit(autoCommit);
        }
        if (defaultTransactionIsolationLevel != null) {
            conn.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
    }

    @AllArgsConstructor
    private static class DriverProxy implements Driver {
        private final Driver driver;

        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }

        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }

        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }

        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        public Logger getParentLogger() {
            return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        }
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // requires JDK version 1.6
    }

}