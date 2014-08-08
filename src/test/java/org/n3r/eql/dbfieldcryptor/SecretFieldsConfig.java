package org.n3r.eql.dbfieldcryptor;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class SecretFieldsConfig implements SecretFieldsConfigable {
    @Override
    public Set<String> getSecretFieldsConfig() {
        return ImmutableSet.of("ESQL_SECRET_TEST.C", "ESQL_SECRET_TEST.B");
    }
}
