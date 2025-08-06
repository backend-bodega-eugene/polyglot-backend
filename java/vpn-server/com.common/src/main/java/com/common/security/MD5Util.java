//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.security;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class MD5Util {

    public static String encrypt(String content) {
        return DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8));
    }

    public static String encrypt(String content, String key) {
        return encrypt(content + key);
    }

}
