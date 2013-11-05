package org.n3r.eql.config;

public class EqlJndiConfig implements EqlConfig {
    private String jndiName, initial, url;
    private String transactionType;

    public EqlJndiConfig(String jndiName, String initial, String url, String transactionType) {
        this.jndiName = jndiName;
        this.initial = initial;
        this.url = url;
        this.transactionType = transactionType;
    }

    @Override
    public String getStr(String key) {
        if (EqlConfigKeys.JNDI_NAME.equals(key)) return jndiName;
        if (EqlConfigKeys.INITIAL.equals(key)) return initial;
        if (EqlConfigKeys.PROVIDER_URL.equals(key)) return url;
        if (EqlConfigKeys.TRANSACTION_TYPE.equals(key)) return transactionType;  // JTA or not

        return null;
    }

}
