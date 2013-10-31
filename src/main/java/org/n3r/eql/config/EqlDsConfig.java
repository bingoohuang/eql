package org.n3r.eql.config;

import com.google.common.base.Strings;
import org.n3r.eql.EqlTran;
import org.n3r.eql.ex.EqlConfigException;
import org.n3r.eql.trans.EqlJdbcTransaction;
import org.n3r.eql.trans.EqlJtaTransaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

public class EqlDsConfig implements EqlTranAware {
    private String transactionType;
    private String jndiName;
    private String initial;
    private String url;
    private DataSource dataSource;

    private void createDataSource() {
        try {
            Hashtable<String, String> context = new Hashtable<String, String>();
            if (!Strings.isNullOrEmpty(url)) context.put("java.naming.provider.url", url);
            if (!Strings.isNullOrEmpty(initial)) context.put("java.naming.factory.initial", initial);

            dataSource = (DataSource) new InitialContext(context).lookup(jndiName);
        } catch (NamingException e) {
            throw new EqlConfigException("create data source fail", e);
        }
    }

    public Connection getConnection() {
        try {
            if (dataSource == null) createDataSource();

            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new EqlConfigException("create connection fail", e);
        }
    }

    @Override
    public EqlTran getTran() {
        if ("jta".equals(transactionType)) return new EqlJtaTransaction();

        return new EqlJdbcTransaction(getConnection());
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
