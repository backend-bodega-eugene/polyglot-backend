package com.eugene.utils;

import java.util.Set;

public class ChineseIDValidator {

    // 系数表
    private static final int[] WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3,
            7, 9, 10, 5, 8, 4, 2};

    // 校验码对照表
    private static final char[] CHECK_CODE = {'1', '0', 'X', '9', '8', '7',
            '6', '5', '4', '3', '2'};

    // 简化省份代码前两位合法列表（不是完整省份字典）
    private static final Set<String> VALID_PROVINCES = Set.of(
            "11", "12", "13", "14", "15", "21", "22", "23",
            "31", "32", "33", "34", "35", "36", "37", "41",
            "42", "43", "44", "45", "46", "50", "51", "52",
            "53", "54", "61", "62", "63", "64", "65", "71",
            "81", "82"
    );

    /**
     * 主方法：校验身份证
     */
    public static boolean isValid(String id) {
        if (id == null || id.length() != 18) return false;

        // 1. 校验省份码
        String province = id.substring(0, 2);
        if (!VALID_PROVINCES.contains(province)) return false;

        // 2. 校验出生日期
        String birthday = id.substring(6, 14);
        if (!isValidDate(birthday)) return false;

        // 3. 校验前17位是否全为数字
        String body = id.substring(0, 17);
        if (!body.matches("\\d+")) return false;

        // 4. 校验码比对
        char expectedCheckCode = calculateCheckCode(body);
        char actualCheckCode = Character.toUpperCase(id.charAt(17));

        return expectedCheckCode == actualCheckCode;
    }

    /**
     * 校验出生日期是否合法
     */
    private static boolean isValidDate(String dateStr) {
        try {
            java.time.LocalDate.parse(dateStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 计算校验码
     */
    private static char calculateCheckCode(String body) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Character.getNumericValue(body.charAt(i)) * WEIGHT[i];
        }
        int mod = sum % 11;
        return CHECK_CODE[mod];
    }

    /**
     * 提取性别（奇数男，偶数女）
     */
    public static String getGender(String id) {
        if (!isValid(id)) return "未知";
        int genderCode = Character.getNumericValue(id.charAt(16));
        return (genderCode % 2 == 0) ? "女" : "男";
    }

    /**
     * 提取出生日期
     */
    public static String getBirthdate(String id) {
        if (!isValid(id)) return "未知";
        return id.substring(6, 10) + "-" + id.substring(10, 12) + "-" + id.substring(12, 14);
    }
}
