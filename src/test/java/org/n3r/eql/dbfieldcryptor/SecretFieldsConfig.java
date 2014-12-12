package org.n3r.eql.dbfieldcryptor;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class SecretFieldsConfig implements SecretFieldsConfigable {
    @Override
    public Set<String> getSecretFieldsConfig() {
        return ImmutableSet.of("EQL_SECRET_TEST.C", "EQL_SECRET_TEST.B");
    }
}
