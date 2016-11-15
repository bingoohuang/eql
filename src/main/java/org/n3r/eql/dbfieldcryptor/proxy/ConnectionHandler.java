package org.n3r.eql.dbfieldcryptor.proxy;

import lombok.val;
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

    public ConnectionHandler(
            Connection connection,
            SensitiveCryptor cryptor,
            ParserCache parserCache,
            DbDialect dbDialect) {
        this.connection = connection;
        this.cryptor = cryptor;
        this.parserCache = parserCache;
        this.dbDialect = dbDialect;
    }

    @Override
    public Object invoke(
            Object proxy,
            Method method,
            Object[] args) throws Throwable {
        SensitiveFieldsParser parser = null;
        val methodName = method.getName();
        if (O.in(methodName, "prepareStatement", "prepareCall")) {
            val sql = (String) args[0];
            parser = parserCache.getParser(dbDialect, sql);
            if (parser != null) args[0] = parser.getSql();
        }

        val invoke = method.invoke(connection, args);

        if (parser == null) return invoke;

        if ("prepareStatement".equals(methodName)) {
            val ps = (PreparedStatement) invoke;
            val stmtHandler = new PreparedStmtHandler(ps, parser, cryptor);
            return stmtHandler.createPreparedStatementProxy();
        } else if ("prepareCall".equals(methodName)) {
            val cs = (CallableStatement) invoke;
            val stmtHandler = new CallableStmtHandler(cs, parser, cryptor);
            return stmtHandler.createCallableStatement();
        }

        return invoke;
    }

    public Connection createConnectionProxy() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Connection.class}, this);
    }
}
