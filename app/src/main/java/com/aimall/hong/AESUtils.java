package com.aimall.hong;


import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class AESUtils {

    public static byte[] encrypt(byte[] content, String password) {
        try {
            byte[] keyStr = password.getBytes();
            SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//algorithmStr
            cipher.init(Cipher.ENCRYPT_MODE, key);//   ʼ
            byte[] result = cipher.doFinal(content);
            return result; //
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] content, String password) {
        try {
            byte[] keyStr = password.getBytes();
            SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//algorithmStr
            cipher.init(Cipher.DECRYPT_MODE, key);//   ʼ
            return cipher.doFinal(content); //
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }


}