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
        if ("jndiName".equals(key)) return jndiName;
        if ("java.naming.factory.initial".equals(key)) return initial;
        if ("java.naming.provider.url".equals(key)) return url;
        if ("transactionType".equals(key)) return transactionType;  // JTA or not

        return null;
    }

}
