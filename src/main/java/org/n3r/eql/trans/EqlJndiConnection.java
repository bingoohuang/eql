package org.n3r.eql.trans;

import lombok.SneakyThrows;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.S;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Hashtable;

public class EqlJndiConnection extends AbstractEqlConnection {
    private DataSource dataSource;

    @Override @SneakyThrows
    public Connection getConnection(String dbName) {
        return dataSource.getConnection();
    }

    @Override
    public void destroy() {
        dataSource = null;
    }

    @Override
    public String getDriverName() {
        return EqlUtils.getDriverNameFromConnection(dataSource);
    }

    @Override
    public String getJdbcUrl() {
        return EqlUtils.getJdbcUrlFromConnection(dataSource);
    }

    @Override
    public void initialize(EqlConfig eqlConfig) {
        String jndiName = eqlConfig.getStr("jndiName");
        String initial = eqlConfig.getStr("java.naming.factory.initial");
        String url = eqlConfig.getStr("java.naming.provider.url");

        createDataSource(jndiName, initial, url);
    }

    @SneakyThrows
    private void createDataSource(String jndiName, String initial, String url) {
        Hashtable<String, String> context = new Hashtable<>();
        if (S.isNotEmpty(url)) context.put("java.naming.provider.url", url);
        if (S.isNotEmpty(initial))
            context.put("java.naming.factory.initial", initial);

        dataSource = (DataSource) new InitialContext(context).lookup(jndiName);
    }

}
