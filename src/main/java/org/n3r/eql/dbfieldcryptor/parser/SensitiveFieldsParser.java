package org.n3r.eql.dbfieldcryptor.parser;

import java.util.Set;

public interface SensitiveFieldsParser {
    Set<Integer> getSecureBindIndices();

    Set<Integer> getSecureResultIndices();

    Set<String> getSecureResultLabels();

    boolean inBindIndices(int index);

    boolean inResultIndices(int index);

    boolean inResultLabels(String label);

    boolean inResultIndicesOrLabel(Object indexOrLabel);

    boolean haveNonSecureFields();

    String getSql();
}
