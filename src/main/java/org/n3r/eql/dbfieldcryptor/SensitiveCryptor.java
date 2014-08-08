package org.n3r.eql.dbfieldcryptor;

public interface SensitiveCryptor {
    String encrypt(String data);
    String decrypt(String data);
}
