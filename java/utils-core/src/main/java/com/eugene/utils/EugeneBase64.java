package com.eugene.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Base64 编码实现（不依赖标准库）
 *
 * 原理：
 * - 每3个字节共24位，拆成4组，每组6位
 * - 6位范围是0~63，对应64个字符表
 * - 不足3字节补0，结果用"="补位填满4个字符
 *
 * 例："abc" → [01100001 01100010 01100011]
 *   → 011000 010110 001001 100011
 *   → index: 24 22 9 35 → "YWJj"
 *
 * 特点：
 * - 可移植（语言无关）
 * - 可逆（可写解码方法还原）
 */
public class EugeneBase64 {

    //64个字符
    private static final char[] BASE64_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();


    public static String myBase64Encode(String input) {
        return myBase64Encode(input, StandardCharsets.UTF_8);
    }
    public static String myBase64Encode(String input, Charset charset) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return myBase64Encode(input.getBytes(charset));
    }
    public static String myBase64DecodeToString(String base64) {
        return myBase64DecodeToString(base64, StandardCharsets.UTF_8);
    }

    public static String myBase64DecodeToString(String base64, Charset charset) {
        return new String(myBase64Decode(base64), charset);
    }

    public static String myBase64Encode(byte[] data) {
        StringBuilder result = new StringBuilder();

        int i = 0;
        while (i < data.length) {
            // 每3个字节为一组
            int byte1 = data[i++] & 0xFF;
            int byte2 = (i < data.length) ? data[i++] & 0xFF : 0;
            int byte3 = (i < data.length) ? data[i++] & 0xFF : 0;

            // 把三个字节拼成24位
            int combined = (byte1 << 16) | (byte2 << 8) | byte3;

            // 分成4个6位数字，查表输出
            int index1 = (combined >> 18) & 0x3F;
            int index2 = (combined >> 12) & 0x3F;
            int index3 = (combined >> 6) & 0x3F;
            int index4 = combined & 0x3F;

            result.append(BASE64_TABLE[index1]);
            result.append(BASE64_TABLE[index2]);

            // 判断 padding（补=）情况
            if (i - 1 < data.length) {
                result.append(BASE64_TABLE[index3]);
            } else {
                result.append('=');
            }

            if (i < data.length) {
                result.append(BASE64_TABLE[index4]);
            } else {
                result.append('=');
            }
        }

        return result.toString();
    }
    public static byte[] myBase64Decode(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new byte[0];
        }

        // 反查表：ASCII -> index
        int[] reverseTable = new int[128];
        Arrays.fill(reverseTable, -1);
        for (int i = 0; i < BASE64_TABLE.length; i++) {
            reverseTable[BASE64_TABLE[i]] = i;
        }

        // 去掉所有非 base64 字符 & 统计有效字符
        StringBuilder cleaned = new StringBuilder();
        for (char c : base64.toCharArray()) {
            if (c == '=' || (c < 128 && reverseTable[c] != -1)) {
                cleaned.append(c);
            }
        }

        int len = cleaned.length();
        int fullGroups = len / 4;
        int outputLen = fullGroups * 3;

        if (cleaned.charAt(len - 1) == '=') outputLen--;
        if (cleaned.charAt(len - 2) == '=') outputLen--;

        byte[] output = new byte[outputLen];
        int outIndex = 0;

        for (int i = 0; i < fullGroups; i++) {
            int index = i * 4;
            int c1 = reverseTable[cleaned.charAt(index)];
            int c2 = reverseTable[cleaned.charAt(index + 1)];
            int c3 = cleaned.charAt(index + 2) == '=' ? 0 : reverseTable[cleaned.charAt(index + 2)];
            int c4 = cleaned.charAt(index + 3) == '=' ? 0 : reverseTable[cleaned.charAt(index + 3)];

            int combined = (c1 << 18) | (c2 << 12) | (c3 << 6) | c4;

            output[outIndex++] = (byte) ((combined >> 16) & 0xFF);
            if (cleaned.charAt(index + 2) != '=') {
                output[outIndex++] = (byte) ((combined >> 8) & 0xFF);
            }
            if (cleaned.charAt(index + 3) != '=') {
                output[outIndex++] = (byte) (combined & 0xFF);
            }
        }

        return output;
    }
    public static void main(String[] args) {
        String encoded = myBase64Encode("hello,Eugene");
        String decoded = myBase64DecodeToString(encoded);
        System.out.println("原始字符串: " + decoded);
    }
}
