package com.common.sign;

import com.common.security.HexUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public interface ServiceSignature {
    ThreadLocal<MessageDigest> DIGEST = ThreadLocal.withInitial(ServiceSignature::initialValue);

    private static MessageDigest initialValue() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException var1) {
            throw new RuntimeException(var1);
        }
    }

    static String getSignVal(String content) {
        Objects.requireNonNull(content);
        char[] hex = HexUtil.encodeHex(((MessageDigest) DIGEST.get()).digest(content.getBytes(StandardCharsets.UTF_8)), false);
        return new String(hex);
    }
}