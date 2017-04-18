package org.n3r.eql.config;

public interface EqlConfigKeys {
    String CONNECTION_IMPL = "connection.impl";
    String PROXY_CONNECTION_IMPL = "proxy.connection.impl";
    String SECRET_FIELDS_CONFIGABLE_IMPL = "secret.fields.configable.impl";
    String SENSITIVE_CRYPTOR_IMPL = "sensitive.cryptor.impl";

    String JNDI_NAME = "jndiName";
    String DRIVER = "driver";
    String URL = "url";
    String USER = "user";
    String PASSWORD = "password";
    String INITIAL = "java.naming.factory.initial";
    String PROVIDER_URL = "java.naming.provider.url";
    String TRANSACTION_TYPE = "transactionType";  // JTA or JDBC
    String JTA = "JTA";
    String EXPRESSION_EVALUATOR = "expression.evaluator";
    String SQL_RESOURCE_LOADER = "sql.resource.loader";
    String DYNAMIC_LANGUAGE_DRIVER = "dynamic.language.driver";

    String SQL_PARSE_LAZY = "sql.parse.lazy";
}
