package org.n3r.eql.config;

import com.google.common.collect.ImmutableMap;
import lombok.Value;

import java.util.Map;

@Value
public class EqlJndiConfig implements EqlConfig {
    private String jndiName, initial, url;
    private String transactionType;

    @Override
    public String getStr(String key) {
        if (EqlConfigKeys.JNDI_NAME.equals(key)) return jndiName;
        if (EqlConfigKeys.INITIAL.equals(key)) return initial;
        if (EqlConfigKeys.PROVIDER_URL.equals(key)) return url;
        if (EqlConfigKeys.TRANSACTION_TYPE.equals(key))
            return transactionType;  // JTA or not

        return null;
    }

    @Override
    public Map<String, String> params() {
        return ImmutableMap.of("jndiName", jndiName, "initial", initial,
                "url", url, "transactionType", transactionType);
    }
}
