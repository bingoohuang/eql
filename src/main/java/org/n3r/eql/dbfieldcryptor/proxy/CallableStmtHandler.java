package org.n3r.eql.dbfieldcryptor.proxy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.n3r.eql.dbfieldcryptor.SensitiveCryptor;
import org.n3r.eql.dbfieldcryptor.parser.SensitiveFieldsParser;
import org.n3r.eql.util.O;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.CallableStatement;

import static java.lang.reflect.Proxy.newProxyInstance;

@Slf4j @AllArgsConstructor
class CallableStmtHandler implements InvocationHandler {
    private final CallableStatement stmt;
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
                log.warn("Encrypt parameter #{}# error", args[1]);
            }
            return method.invoke(stmt, args);
        }

        if (O.in(method.getName(), "getString", "getObject")
                && parser.inBindIndices((Integer) args[0])) {
            try {
                Object result = method.invoke(stmt, args);
                return result != null ? cryptor.decrypt("" + result) : result;
            } catch (Exception e) {
                log.warn("Decrypt parameter #{}# error", args[1]);
            }
        }

        return method.invoke(stmt, args);
    }


    public CallableStatement createCallableStatement() {
        return (CallableStatement) newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{CallableStatement.class}, this);
    }
}
