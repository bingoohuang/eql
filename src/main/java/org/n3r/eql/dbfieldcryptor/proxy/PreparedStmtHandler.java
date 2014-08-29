package org.n3r.eql.dbfieldcryptor.proxy;

import org.n3r.eql.dbfieldcryptor.SensitiveCryptor;
import org.n3r.eql.dbfieldcryptor.parser.SensitiveFieldsParser;
import org.n3r.eql.util.O;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

class PreparedStmtHandler implements InvocationHandler {
    private final Logger logger = LoggerFactory.getLogger(PreparedStmtHandler.class);

    private final PreparedStatement pstmt;
    private final SensitiveCryptor cryptor;
    private final SensitiveFieldsParser parser;

    public PreparedStmtHandler(PreparedStatement pStmt, SensitiveFieldsParser parser, SensitiveCryptor cryptor) {
        this.pstmt = pStmt;
        this.parser = parser;
        this.cryptor = cryptor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (O.in(method.getName(), "setString", "setObject")
                && parser.inBindIndice((Integer) args[0])) {
            try {
                args[1] = args[1] == null ? null : cryptor.encrypt(args[1].toString());
            } catch (Exception e) {
                logger.warn("Encrypt parameter #{}# error", args[1]);
            }
        }

        Object result = method.invoke(pstmt, args);

        if (O.in(method.getName(), "executeQuery", "getResultSet")
                && parser.getSecuretResultIndice().size() > 0) {
            result = new ResultSetHandler((ResultSet) result, parser, cryptor).createResultSetProxy();
        }

        return result;
    }

    public PreparedStatement createPreparedStatementProxy() {
        return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{PreparedStatement.class}, this);
    }
}
