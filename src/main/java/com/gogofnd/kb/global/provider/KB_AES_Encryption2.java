package com.gogofnd.kb.global.provider;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class KB_AES_Encryption2 {

    private static volatile KB_AES_Encryption2 INSTANCE;

    final static String secretKey = "1vE3ePU5VGb8u0wN99yzy0r8DAgxs5vZ"; // 32bit
    static String IV = "1vE3ePU5VGb8u0wN"; // 16bit

    public static KB_AES_Encryption2 getInstance() {
        if (INSTANCE == null) {
            synchronized (KB_AES_Encryption2.class) {
                if (INSTANCE == null)
                    INSTANCE = new KB_AES_Encryption2();
            }
        }
        return INSTANCE;
    }

    public KB_AES_Encryption2() {
        IV = secretKey.substring(0, 16);
    }

    // 암호화
    public static String encrypt(String str)
            throws Exception {
        byte[] keyData = secretKey.getBytes();

        SecretKey secureKey = new SecretKeySpec(keyData, "AES");

        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes()));

        byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
        String enStr = new String(Base64.encodeBase64(encrypted));

        return enStr;
    }

    // 복호화
    public static String decrypt(String str)
            throws Exception {
        byte[] keyData = secretKey.getBytes();
        SecretKey secureKey = new SecretKeySpec(keyData, "AES");
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes("UTF-8")));

        byte[] byteStr = Base64.decodeBase64(str.getBytes());

        return new String(c.doFinal(byteStr), "UTF-8");
    }
}

