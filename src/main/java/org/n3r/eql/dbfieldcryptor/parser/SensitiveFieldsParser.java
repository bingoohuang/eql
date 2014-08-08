package org.n3r.eql.dbfieldcryptor.parser;

import java.util.Set;

public interface SensitiveFieldsParser {
    Set<Integer> getSecuretBindIndice();

    Set<Integer> getSecuretResultIndice();

    Set<String> getSecuretResultLabels();

    boolean inBindIndice(int index);

    boolean inResultIndice(int index);

    boolean inResultLables(String label);

    boolean inResultIndiceOrLabel(Object indexOrLabel);

    boolean haveNotSecureFields();

    String getSql();
}
