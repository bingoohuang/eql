package org.n3r.eql.trans;

import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.trans.EqlConnection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import static org.n3r.eql.util.EqlUtils.isNotEmpty;

public class EqlJndiConnection implements EqlConnection {
    private DataSource dataSource;

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new EqlConfigException("create connection fail", e);
        }
    }

    @Override
    public void initialize(EqlConfig eqlConfig) {
        String jndiName = eqlConfig.getStr("jndiName");
        String initial = eqlConfig.getStr("java.naming.factory.initial");
        String url = eqlConfig.getStr("java.naming.provider.url");

        createDataSource(jndiName, initial, url);
    }


    private void createDataSource(String url, String initial, String jndiName) {
        try {
            Hashtable<String, String> context = new Hashtable<String, String>();
            if (isNotEmpty(url)) context.put("java.naming.provider.url", url);
            if (isNotEmpty(initial)) context.put("java.naming.factory.initial", initial);

            dataSource = (DataSource) new InitialContext(context).lookup(jndiName);
        } catch (NamingException e) {
            throw new EqlConfigException("create data source fail", e);
        }
    }

}
