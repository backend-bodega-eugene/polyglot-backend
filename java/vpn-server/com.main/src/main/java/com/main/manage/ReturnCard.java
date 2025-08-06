package com.main.manage;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReturnCard {

    private ReturnCard() {


    }

    private static ReturnCard myReturnCard;

    public static ReturnCard GetSingleton() {
        if (null == myReturnCard) {
            myReturnCard = new ReturnCard();
        }
        return myReturnCard;
    }

    public String GetReturnRodom(int length) {
        Random rand = new Random();//生成随机数
        String cardNnumer = "";
        for (int a = 0; a < length; a++) {
            cardNnumer += rand.nextInt(10);//对应length位数
        }
        return cardNnumer;
    }

    private final static String LINE_IVS = "1111000011001111";
    private final static String LINE_KEY = "1111011011000001";

    public static synchronized String LineAESEncrypt(String content) {
        return AESEncrypt(content, LINE_KEY, LINE_IVS);

    }

    public static synchronized String LineAESDncrypt(String content) {
        return AESDecrypt(content, LINE_KEY, LINE_IVS);

    }

    public static String ReturnDatetimeAndGuid() {

        String guid = UUID.randomUUID().toString().replace("-", "").toString();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateformatStr = format.format(System.currentTimeMillis());
        return dateformatStr + guid;
    }

    private static String AESEncrypt(String content, String key, String ivs) {
        try {
            byte[] contentByte = content.getBytes("UTF-8");
            byte[] keyBytes = key.getBytes("UTF-8");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            byte[] initParam = ivs.getBytes("UTF-8");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
            byte[] resultByte = cipher.doFinal(contentByte);
            String result = Base64.getEncoder().encodeToString(resultByte);
            return result;
        } catch (UnsupportedEncodingException | NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String AESDecrypt(String content, String key, String ivs) {
        try {
            byte[] contentByte = Base64.getDecoder().decode(content);//   Base64Utils.decode(content);
            byte[] keyBytes = key.getBytes("UTF-8");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            byte[] initParam = ivs.getBytes("UTF-8");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
            byte[] resultByte = cipher.doFinal(contentByte);
            String result = new String(resultByte, "UTF-8");
            return result;
        } catch (UnsupportedEncodingException | NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String code1 = "admin";
        String code2 = LineAESEncrypt(code1);
        System.out.println(code2);
        System.out.println(LineAESDncrypt(code2));
    }

}

