package org.n3r.eql.dbfieldcryptor.refer;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import lombok.val;
import org.n3r.eql.dbfieldcryptor.EqlSecretFieldsConnectionProxy;
import org.n3r.eql.dbfieldcryptor.SecretFieldsConfigable;
import org.n3r.eql.util.S;

import java.util.Set;

public class ReferSecretFieldsConfig implements SecretFieldsConfigable {
    private Set<String> secretFieldsConfig;

    public ReferSecretFieldsConfig() {
        val eqlConfig = EqlSecretFieldsConnectionProxy.threadLocal.get();
        if (eqlConfig == null) return;

        val secureDatabaseFields = eqlConfig.getStr("securetDatabaseFields.define");
        if (S.isBlank(secureDatabaseFields)) return;

        val splitter = Splitter.onPattern("\\s+").trimResults().omitEmptyStrings();
        Iterable<String> secureFields = splitter.split(secureDatabaseFields.toUpperCase());

        secretFieldsConfig = Sets.newHashSet(secureFields);
    }

    @Override
    public Set<String> getSecretFieldsConfig() {
        return secretFieldsConfig;
    }
}
