package org.n3r.eql.dbfieldcryptor.proxy;

import org.n3r.eql.DbDialect;
import org.n3r.eql.dbfieldcryptor.SensitiveCryptor;
import org.n3r.eql.dbfieldcryptor.parser.ParserCache;
import org.n3r.eql.dbfieldcryptor.parser.SensitiveFieldsParser;
import org.n3r.eql.util.O;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ConnectionHandler implements InvocationHandler {
    private final ParserCache parserCache;
    private final Connection connection;
    private final SensitiveCryptor cryptor;
    private final DbDialect dbDialect;

    public ConnectionHandler(Connection connection, SensitiveCryptor cryptor, ParserCache parserCache, DbDialect dbDialect) {
        this.connection = connection;
        this.cryptor = cryptor;
        this.parserCache = parserCache;
        this.dbDialect = dbDialect;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        SensitiveFieldsParser parser = null;
        String methodName = method.getName();
        if (O.in(methodName, "prepareStatement", "prepareCall")) {
            String sql = (String) args[0];
            parser = parserCache.getParser(dbDialect, sql);
            if (parser != null) args[0] = parser.getSql();
        }

        Object invoke = method.invoke(connection, args);

        if (parser == null) return invoke;

        if ("prepareStatement".equals(methodName)) {
            PreparedStatement ps = (PreparedStatement) invoke;
            PreparedStmtHandler stmtHandler = new PreparedStmtHandler(ps, parser, cryptor);
            return stmtHandler.createPreparedStatementProxy();
        } else if ("prepareCall".equals(methodName)) {
            CallableStatement cs = (CallableStatement) invoke;
            CallableStmtHandler stmtHandler = new CallableStmtHandler(cs, parser, cryptor);
            return stmtHandler.createCallableStatement();
        }

        return invoke;
    }

    public Connection createConnectionProxy() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Connection.class}, this);
    }
}
