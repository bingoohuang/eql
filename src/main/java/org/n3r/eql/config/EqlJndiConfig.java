package org.n3r.eql.config;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

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

    @Override
    public Map<String, String> params() {
        return ImmutableMap.of("jndiName", jndiName, "initial", initial, "url", url, "transactionType", transactionType);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EqlJndiConfig that = (EqlJndiConfig) o;

        if (initial != null ? !initial.equals(that.initial) : that.initial != null) return false;
        if (!jndiName.equals(that.jndiName)) return false;
        if (transactionType != null ? !transactionType.equals(that.transactionType) : that.transactionType != null)
            return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jndiName.hashCode();
        result = 31 * result + (initial != null ? initial.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (transactionType != null ? transactionType.hashCode() : 0);
        return result;
    }
}
