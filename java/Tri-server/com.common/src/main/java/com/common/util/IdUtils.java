package com.common.util;

public class IdUtils {
    private static SnowFlake snowFlake;

    static {
        snowFlake = new SnowFlake(1, 1);
    }

    public static String generateId() {
        return snowFlake.nextId().toString();
    }
}
