package org.n3r.eql.dbfieldcryptor.proxy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.n3r.eql.dbfieldcryptor.SensitiveCryptor;
import org.n3r.eql.dbfieldcryptor.parser.SensitiveFieldsParser;
import org.n3r.eql.util.O;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static java.lang.reflect.Proxy.newProxyInstance;

@Slf4j @AllArgsConstructor
class PreparedStmtHandler implements InvocationHandler {
    private final PreparedStatement pstmt;
    private final SensitiveFieldsParser parser;
    private final SensitiveCryptor cryptor;

    @Override
    public Object invoke(
            Object proxy,
            Method method,
            Object[] args) throws Throwable {
        if (O.in(method.getName(), "setString", "setObject")
                && parser.inBindIndices((Integer) args[0])) {
            try {
                if (args[1] != null)
                    args[1] = cryptor.encrypt(args[1].toString());
            } catch (Exception e) {
                log.warn("Encrypt parameter #{}# error", args[1], e);
            }
        }

        Object result = method.invoke(pstmt, args);

        if (O.in(method.getName(), "executeQuery", "getResultSet")
                && parser.getSecureResultIndices().size() > 0) {
            result = new ResultSetHandler((ResultSet) result, parser, cryptor)
                    .createResultSetProxy();
        }

        return result;
    }

    public PreparedStatement createPreparedStatementProxy() {
        return (PreparedStatement) newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{PreparedStatement.class}, this);
    }
}
