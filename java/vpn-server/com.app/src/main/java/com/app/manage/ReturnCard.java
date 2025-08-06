package com.app.manage;

import com.alibaba.druid.sql.visitor.functions.Substring;
import com.common.util.StringUtils;

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

    public String ReturnRodom(List<String> current, Integer length) {
        Random rand = new Random();//生成随机数
        String cardNnumer = "";
        for (int a = 0; a < length; a++) {
            cardNnumer += rand.nextInt(10);//根据length生成位数
        }
        if (current.contains(cardNnumer)) {
            ReturnRodom(current, length);
        }
        return cardNnumer;
    }

    public String GetReturnRodom(int length) {
        Random rand = new Random();//生成随机数
        String cardNnumer = "";
        for (int a = 0; a < length; a++) {
            cardNnumer += rand.nextInt(10);//对应length位数
        }
        return cardNnumer;
    }

    public String ReturnDatetimeAndGuid() {

        String guid = UUID.randomUUID().toString().replace("-", "").toString();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateformatStr = format.format(System.currentTimeMillis());
        return dateformatStr + guid;
    }

    public boolean IsMobile(final String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][0-9][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    public boolean IsEmail(String emailStr) {

        String regex = "@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(emailStr);
        boolean b = m.matches();
        return b;
    }

    public LocalDateTime ConverDateRemoveTime(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentdatetime = LocalDateTime.parse(date.format(formatter));
        return currentdatetime;
    }

    public String AESEncrypt(String content, String key) {
        String ivs = "1111000000000000";
        return AESEncrypt(content, key, ivs);
    }

    public String AESDecrypt(String content, String key) {
        String ivs = "1111000000000000";
        return AESDecrypt(content, key, ivs);
    }

    public String VmessAESEncrypt(String content) {
        String ivs = "1111000001110000";
        String key = "1111000001110000";
        return AESEncrypt(content, key, ivs);
    }

    public String VmessAESDecrypt(String content) {
        String ivs = "1111000001110000";
        String key = "1111000001110000";
        return AESDecrypt(content, key, ivs);
    }

    private String AESEncrypt(String content, String key, String ivs) {
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

    private String AESDecrypt(String content, String key, String ivs) {
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

    private final static String memberSessionKey = "0000101110001111";

    public String EncodeSession(String session) {
        return AESEncrypt(session, memberSessionKey);
    }

    public String DecodeSession(String session) {
        return AESDecrypt(session, memberSessionKey);
    }

    private final static String LINE_IVS = "1111000011001111";
    private final static String LINE_KEY = "1111011011001111";

    public synchronized String LineAESEncrypt(String content) {
        return AESEncrypt(content, LINE_KEY, LINE_IVS);
    }

    public String LineAESDncrypt(String content) {
        return AESDecrypt(content, LINE_KEY, LINE_IVS);
    }

    public Map<Integer, String> myMap = new HashMap<Integer, String>();

    public String GetMap(Integer mapkey) {
        if (myMap.size() <= 0) {
            //0:ios,1:android,2:windows,3:macOS,4:Linux
            myMap.put(0, "ios");
            myMap.put(1, "Android");
            myMap.put(2, "windows");
            myMap.put(3, "macOS");
            myMap.put(4, "Linux");
        }
        return myMap.get(mapkey);
    }

    public String verifyStr(String str) {
        if (StringUtils.isMobileNO(str)) {
            return str.substring(0, 3) + "****" + str.substring(7, str.length());
        } else if (StringUtils.isEmail(str)) {
            str = str.substring(0, str.indexOf("@"));
            if (str.length() > 3 && str.length() < 7) {
                str = str.substring(0, 3) + "***";
            }
            if (str.length() > 7) {
                str = str.substring(0, 3) + "***" + str.substring(str.length()-4, str.length());

            }
            if (str.length() <= 3) {

                str = str.substring(0, str.length()) + "***";
            }

        }
        return str;
    }
    public static void main(String[] args) throws UnsupportedEncodingException {

       ReturnCard.GetSingleton().verifyStr("l3333333333a@gmail.com");
//        String code1 = "1,2,3,4,";
//        String[] codearr=code1.split(",");
//        if(codearr.)
//        code1=code1.substring(0,code1.length() - 2);
//
//        System.out.println(code1);
    }

}
//        String cryptKey = "aPb4x9q0H4W8rPs7aPb4x9q0H4W8r@s7";
//        String data = "hello world";
//        data = append(data);
//        AES aes = new AES();
//        aes.setKey(cryptKey);
//        byte[] t1 = aes.encrypt(data.getBytes());
//        System.out.println("data = " + Util.toHEX1(t1));
//        byte[] t2 = aes.decrypt(t1);
//        System.out.println("data = " + new String(t2));

//    public static synchronized String LineAESEncrypt(String content){
//        return AESEncrypt(content, LINE_KEY, LINE_IVS);
//
//    }
//    public static String LineAESDncrypt(String content){
//        return AESDecrypt(content, LINE_KEY, LINE_IVS);
//
//    }
//    private static String AESEncrypt(String content, String key, String ivs)  {
//        try{
//            byte[] contentByte = content.getBytes("UTF-8");
//            byte[] keyBytes = key.getBytes("UTF-8");
//            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
//            byte[] initParam = ivs.getBytes("UTF-8");
//            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
//            byte[] resultByte = cipher.doFinal(contentByte);
//            String result = Base64.getEncoder().encodeToString(resultByte);
//            return result;
//        }catch (UnsupportedEncodingException | NoSuchPaddingException | NoSuchAlgorithmException |
//                InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException |
//                BadPaddingException e){
//            throw new RuntimeException(e);
//        }
//    }
//    private static String AESDecrypt(String content, String key, String ivs) {
//        try{
//            byte[] contentByte = Base64.getDecoder().decode(content);//   Base64Utils.decode(content);
//            byte[] keyBytes = key.getBytes("UTF-8");
//            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
//            byte[] initParam = ivs.getBytes("UTF-8");
//            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
//            byte[] resultByte = cipher.doFinal(contentByte);
//            String result = new String(resultByte, "UTF-8");
//            return result;
//        }catch (UnsupportedEncodingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e){
//            throw new RuntimeException(e);
//        }
//    }

