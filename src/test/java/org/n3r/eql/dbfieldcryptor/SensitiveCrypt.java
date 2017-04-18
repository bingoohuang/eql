package org.n3r.eql.dbfieldcryptor;

public class SensitiveCrypt implements SensitiveCryptor {
    @Override
    public String encrypt(String data) {
        return "###" + data;
    }

    @Override
    public String decrypt(String data) {
        return data.substring(3);
    }
}
