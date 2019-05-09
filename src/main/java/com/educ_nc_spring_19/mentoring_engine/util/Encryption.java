package com.educ_nc_spring_19.mentoring_engine.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class Encryption {

    private Encryption() {}

    public static String decrypt(String key, String encryptedMessage)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        // NoSuchAlgorithmException, NoSuchPaddingException
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        // InvalidKeyException
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        // IllegalBlockSizeException, BadPaddingException
        byte[] decrypted = cipher.doFinal(Base64.decodeBase64(encryptedMessage));

        return new String(decrypted);
    }

    public static String encrypt(String key, String message)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        // NoSuchAlgorithmException, NoSuchPaddingException
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        // InvalidKeyException
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        // IllegalBlockSizeException, BadPaddingException
        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.encodeBase64String(encrypted);
    }
}
