package org.n3r.eql.trans;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.util.EqlUtils;
import org.n3r.eql.util.Fucks;
import org.n3r.eql.util.S;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

public class EqlJndiConnection extends AbstractEqlConnection {
    private DataSource dataSource;

    @Override
    public Connection getConnection(String dbName) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw Fucks.fuck(e);
        }
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


    private void createDataSource(String jndiName, String initial, String url) {
        try {
            Hashtable<String, String> context = new Hashtable<String, String>();
            if (S.isNotEmpty(url)) context.put("java.naming.provider.url", url);
            if (S.isNotEmpty(initial)) context.put("java.naming.factory.initial", initial);

            dataSource = (DataSource) new InitialContext(context).lookup(jndiName);
        } catch (NamingException e) {
            throw Fucks.fuck(e);
        }
    }

}
