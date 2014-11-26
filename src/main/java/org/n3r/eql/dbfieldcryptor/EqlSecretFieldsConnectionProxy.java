package org.n3r.eql.dbfieldcryptor;

import org.n3r.eql.DbDialect;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.dbfieldcryptor.parser.ParserCache;
import org.n3r.eql.dbfieldcryptor.proxy.ConnectionHandler;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.trans.EqlConnection;
import org.n3r.eql.util.S;

import java.sql.Connection;
import java.util.Set;

import static org.n3r.eql.config.EqlConfigKeys.*;
import static org.n3r.eql.config.EqlConfigManager.createEqlConnection;

public class EqlSecretFieldsConnectionProxy implements EqlConnection {
    private EqlConnection eqlConnection;
    private ParserCache parserCache;
    private SensitiveCryptor sensitiveCryptor;
    public static ThreadLocal<EqlConfig> threadLocal;

    @Override
    public void initialize(EqlConfig eqlConfig) {
        eqlConnection = createEqlConnection(eqlConfig, PROXY_CONNECTION_IMPL);
        eqlConnection.initialize(eqlConfig);

        threadLocal = new ThreadLocal<EqlConfig>();
        threadLocal.set(eqlConfig);

        String secretFieldsConfigableImpl = eqlConfig.getStr(SECRET_FIELDS_CONFIGABLE_IMPL);
        if (S.isNotEmpty(secretFieldsConfigableImpl)) {
            SecretFieldsConfigable secretFieldsConfigable = Reflect.on(secretFieldsConfigableImpl).create().get();
            Set<String> secretFieldsConfig = secretFieldsConfigable.getSecretFieldsConfig();
            if (secretFieldsConfig != null && !secretFieldsConfig.isEmpty())
                parserCache = new ParserCache(secretFieldsConfig);
        }

        String sensitiveCryptorImpl = eqlConfig.getStr(SENSITIVE_CRYPTOR_IMPL);
        if (S.isNotEmpty(sensitiveCryptorImpl)) {
            sensitiveCryptor = Reflect.on(sensitiveCryptorImpl).create().get();
        }

        threadLocal.remove();
        threadLocal = null;
    }

    @Override
    public String getDbName(EqlConfig eqlConfig, EqlRun eqlRun) {
        return eqlConnection.getDbName(eqlConfig, eqlRun);
    }

    @Override
    public Connection getConnection(String dbName) {
        Connection connection = eqlConnection.getConnection(dbName);
        DbDialect dbDialect = DbDialect.parseDbType(connection);

        if (parserCache == null || sensitiveCryptor == null) return null;
        Connection connectionProxy = new ConnectionHandler(connection, sensitiveCryptor,
                parserCache, dbDialect).createConnectionProxy();
        return connectionProxy;
    }

    @Override
    public void destroy() {
        eqlConnection.destroy();
    }

    @Override
    public String getDriverName() {
        return eqlConnection.getDriverName();
    }
}
