package org.n3r.eql.config;

public interface EqlConfigKeys {
    String CONFIG_CLASS = "configClass";
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
}
