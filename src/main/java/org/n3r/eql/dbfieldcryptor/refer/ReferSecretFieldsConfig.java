package org.n3r.eql.dbfieldcryptor.refer;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.dbfieldcryptor.EqlSecretFieldsConnectionProxy;
import org.n3r.eql.dbfieldcryptor.SecretFieldsConfigable;
import org.n3r.eql.util.S;

import java.util.Set;

public class ReferSecretFieldsConfig implements SecretFieldsConfigable {
    private Set<String> secretFieldsConfig;

    public ReferSecretFieldsConfig() {
        EqlConfig eqlConfig = EqlSecretFieldsConnectionProxy.threadLocal.get();
        if (eqlConfig == null) return;

        String securetDatabaseFields = eqlConfig.getStr("securetDatabaseFields.define");
        if (S.isBlank(securetDatabaseFields)) return;

        Splitter splitter = Splitter.onPattern("\\s+").trimResults().omitEmptyStrings();
        Iterable<String> securetFields = splitter.split(securetDatabaseFields.toUpperCase());

        secretFieldsConfig = Sets.newHashSet(securetFields);
    }

    @Override
    public Set<String> getSecretFieldsConfig() {
        return secretFieldsConfig;
    }
}
