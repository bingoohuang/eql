package org.n3r.eql.dbfieldcryptor;

import org.n3r.eql.DbDialect;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.config.EqlConfigManager;
import org.n3r.eql.dbfieldcryptor.parser.ParserCache;
import org.n3r.eql.dbfieldcryptor.proxy.ConnectionHandler;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.trans.EqlConnection;
import org.n3r.eql.util.EqlUtils;

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
        if (EqlUtils.isNotEmpty(secretFieldsConfigableImpl)) {
            SecretFieldsConfigable secretFieldsConfigable = Reflect.on(secretFieldsConfigableImpl).create().get();
            Set<String> secretFieldsConfig = secretFieldsConfigable.getSecretFieldsConfig();
            if (secretFieldsConfig != null && !secretFieldsConfig.isEmpty())
                parserCache = new ParserCache(secretFieldsConfig);
        }

        String sensitiveCryptorImpl = eqlConfig.getStr(SENSITIVE_CRYPTOR_IMPL);
        if (EqlUtils.isNotEmpty(sensitiveCryptorImpl)) {
            sensitiveCryptor = Reflect.on(sensitiveCryptorImpl).create().get();
        }

        threadLocal.remove();
        threadLocal = null;
    }

    @Override
    public Connection getConnection() {
        Connection connection = eqlConnection.getConnection();
        DbDialect dbDialect = DbDialect.parseDbType(connection);

        if (parserCache == null || sensitiveCryptor == null) return null;
        return new ConnectionHandler(connection, sensitiveCryptor,
                parserCache, dbDialect).createConnectionProxy();
    }
}
