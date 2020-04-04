package org.n3r.eql.dbfieldcryptor.refer.aes;

import lombok.Getter;
import lombok.SneakyThrows;
import org.n3r.eql.util.S;

import javax.crypto.Cipher;
import java.util.Base64;

/**
 * 基础加密解密类.
 *
 * @author Bingoo Huang
 */
public abstract class BaseCryptor {
    /*
     * 加解密密钥.
     */
    @Getter
    String key;

    /**
     * 默认构造函数.
     */
    public BaseCryptor() {
        key = "C152E52CABAB3792";
    }

    /**
     * 带密钥的构造函数.
     *
     * @param key 密钥
     */
    public BaseCryptor(String key) {
        this.key = key == null ? "" : key;
    }

    /**
     * 取得Cipher抽象方法.
     *
     * @param isEncrypt 是否加密
     * @return Cipher对象
     * @throws Exception
     */
    protected abstract Cipher getCipher(boolean isEncrypt);

    /**
     * 加密操作.
     *
     * @param data 需要加密的字符串
     * @return 已经加密的字符串
     */
    @SneakyThrows
    public String encrypt(String data) {
        byte[] cleartext = S.toBytes(data);
        byte[] ciphertext = getCipher(true).doFinal(cleartext);

        return Base64.getEncoder().encodeToString(ciphertext);
    }

    /**
     * 解密操作.
     *
     * @param data 需要解密的字符串
     * @return 解密后的字符串
     */
    @SneakyThrows
    public String decrypt(String data) {
        byte[] cleartext = Base64.getDecoder().decode(data);
        byte[] ciphertext = getCipher(false).doFinal(cleartext);

        return S.bytesToStr(ciphertext);
    }
}
