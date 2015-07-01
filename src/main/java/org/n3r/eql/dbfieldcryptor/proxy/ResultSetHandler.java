package org.n3r.eql.dbfieldcryptor.proxy;

import org.n3r.eql.dbfieldcryptor.SensitiveCryptor;
import org.n3r.eql.dbfieldcryptor.parser.SensitiveFieldsParser;
import org.n3r.eql.util.O;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetHandler implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(ResultSetHandler.class);
    private ResultSet resultSet;
    private SensitiveFieldsParser parser;
    private SensitiveCryptor cryptor;

    public ResultSetHandler(ResultSet resultSet, SensitiveFieldsParser parser,
                            SensitiveCryptor cryptor) throws SQLException {
        this.resultSet = resultSet;
        this.parser = parser;
        this.cryptor = cryptor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(resultSet, args);
        if (result == null) return null;

        if (O.in(method.getName(), "getString", "getObject")
                && parser.inResultIndicesOrLabel(args[0])) {
            try {
                String data = result.toString();
                if (data.length() > 1) result = cryptor.decrypt(data);
            } catch (Exception e) {
                logger.warn("Decrypt result #{}# error", result);
            }
        }

        return result;
    }

    public ResultSet createResultSetProxy() {
        return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ResultSet.class}, this);
    }
}
