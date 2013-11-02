package org.n3r.eql.config;

public class EqlJndiConfig implements EqlConfig{
    private String jndiName, initial, url;
    private String transactionType;

    public EqlJndiConfig() {
    }

    public EqlJndiConfig(String jndiName, String initial, String url, String transactionType) {
        this.jndiName = jndiName;
        this.initial = initial;
        this.url = url;
        this.transactionType = transactionType;
    }
    public EqlJndiConfig(String jndiName, String initial, String url) {
        this(jndiName, initial, url, null);
    }

    @Override
    public String getStr(String key) {
        if("jndiName".equals(key)) return jndiName;
        if("java.naming.factory.initial".equals(key)) return initial;
        if("java.naming.provider.url".equals(key)) return url;
        if("transactionType".equals(key)) return transactionType;  // JTA or not

        return null;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
