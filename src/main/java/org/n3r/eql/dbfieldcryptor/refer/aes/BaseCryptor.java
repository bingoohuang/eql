package org.n3r.eql.dbfieldcryptor.refer.aes;

import org.n3r.eql.util.Fucks;
import org.n3r.eql.util.S;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

/**
 * 基础加密解密类.
 *
 * @author Bingoo Huang
 */
public abstract class BaseCryptor {
    /*
     * 加解密密钥.
     */
    private String key;

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
    public String encrypt(String data) {
        try {
            byte[] cleartext = S.toBytes(data);
            byte[] ciphertext = getCipher(true).doFinal(cleartext);

            return DatatypeConverter.printBase64Binary(ciphertext);
        } catch (Exception e) {
            throw Fucks.fuck(e);
        }

    }

    /**
     * 解密操作.
     *
     * @param data 需要解密的字符串
     * @return 解密后的字符串
     * @throws Exception
     */
    public String decrypt(String data) {
        try {
            byte[] cleartext = DatatypeConverter.parseBase64Binary(data);
            byte[] ciphertext = getCipher(false).doFinal(cleartext);

            return S.bytesToStr(ciphertext);
        } catch (Exception e) {
            throw Fucks.fuck(e);
        }
    }


    /**
     * 获取密钥.
     *
     * @return 密钥
     */
    public String getKey() {
        return key;
    }
}
