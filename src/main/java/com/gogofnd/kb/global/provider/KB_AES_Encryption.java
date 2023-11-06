package com.gogofnd.kb.global.provider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Base64;

public class KB_AES_Encryption {
    static String key = "1vE3ePU5VGb8u0wN99yzy0r8DAgxs5vZ";
    static String key_16 = key.substring(0,16);

    public static String encrypt(String plainText) throws Exception {
        SecretKey keyspec = new SecretKeySpec(key_16.getBytes("UTF-8"), "AES");
        int byteLength = getByteLength(key_16);
        System.out.println("byteLength = " + byteLength);

        //set iv as random 16byte
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        AlgorithmParameterSpec ivspec = new IvParameterSpec(iv);

        // Encryption
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

        int blockSize = 128; //block size
        byte[] dataBytes = plainText.getBytes("UTF-8");

        //find fillChar & pad
        int plaintextLength = dataBytes.length;
        int fillChar = ((blockSize - (plaintextLength % blockSize)));
        plaintextLength += fillChar; //pad

        byte[] plaintext = new byte[plaintextLength];
        Arrays.fill(plaintext, (byte) fillChar);
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

        //encrypt
        byte[] cipherBytes = cipher.doFinal(plaintext);

        //add iv to front of cipherBytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( iv );
        outputStream.write( cipherBytes );

        //encode into base64
        byte [] encryptedIvText = outputStream.toByteArray();
        String result = new String(Base64.getEncoder().encode(encryptedIvText), "UTF-8");
        int length = result.length();
        System.out.println("length = " + length);
        return result;
    }

    public static String decrypt(String encryptedIvText) throws Exception {
        //decode with base64 decoder
        byte [] encryptedIvTextBytes = Base64.getDecoder().decode(encryptedIvText);

        // Extract IV.
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);

        // Extract encrypted part.
        int encryptedSize = encryptedIvTextBytes.length - ivSize;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);



        // Decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKey keyspec = new SecretKeySpec(key_16.getBytes("UTF-8"), "AES");
        AlgorithmParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
        byte[] aesdecode = cipher.doFinal(encryptedBytes);

        // unpad
        byte[] origin = new byte[aesdecode.length - (aesdecode[aesdecode.length - 1])];
        System.arraycopy(aesdecode, 0, origin, 0, origin.length);

        return new String(origin, "UTF-8");
    }

    public static int getByteLength(String str) {
        int strLength = 0;

        char tempChar[] = new char[str.length()];
        for (int i = 0; i < tempChar.length; i++) {

            tempChar[i] = str.charAt(i);
            if (tempChar[i] < 128) {

                strLength++;

            } else {

                strLength += 2;

            }

        }
        return strLength;

    }
}
