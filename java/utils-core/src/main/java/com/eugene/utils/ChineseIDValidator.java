package com.eugene.utils;

import java.util.Set;

/**
 * 工具类：中国大陆居民身份证号校验器（18位）
 * <p>
 * 支持校验身份证格式是否正确、校验码是否匹配、生日是否合法、性别提取等功能。
 * 仅适用于中华人民共和国身份证号（不含港澳台、护照等证件）。
 * </p>
 * 示例：
 * <ul>
 *     <li>{@code isValid("110105199003072617")} 返回 true</li>
 *     <li>{@code getGender("110105199003072617")} 返回 "男"</li>
 *     <li>{@code getBirthdate("110105199003072617")} 返回 "1990-03-07"</li>
 * </ul>
 * <p style="color: red">注意：省份代码校验列表为简化版本，非完整行政区划。</p>
 *
 * @author Eugene
 */
public class ChineseIDValidator {

    /**
     * 加权因子表（前17位数字分别对应的权重）
     */
    private static final int[] WEIGHT = {
            7, 9, 10, 5, 8, 4, 2,
            1, 6, 3, 7, 9, 10, 5,
            8, 4, 2
    };

    /**
     * 校验码对照表（根据加权和模11的结果对应的校验码）
     */
    private static final char[] CHECK_CODE = {
            '1', '0', 'X', '9', '8',
            '7', '6', '5', '4', '3', '2'
    };

    /**
     * 简化的合法省份代码（前两位）
     */
    private static final Set<String> VALID_PROVINCES = Set.of(
            "11", "12", "13", "14", "15", "21", "22", "23",
            "31", "32", "33", "34", "35", "36", "37", "41",
            "42", "43", "44", "45", "46", "50", "51", "52",
            "53", "54", "61", "62", "63", "64", "65", "71",
            "81", "82"
    );

    /**
     * 校验身份证号是否合法（18位）
     *
     * @param id 身份证号（18位）
     * @return 如果身份证格式和校验码正确，返回 true；否则返回 false
     */
    public static boolean isValid(String id) {
        if (id == null || id.length() != 18) return false;

        // 1. 校验省份代码是否合法
        String province = id.substring(0, 2);
        if (!VALID_PROVINCES.contains(province)) return false;

        // 2. 校验出生日期是否合法（yyyyMMdd）
        String birthday = id.substring(6, 14);
        if (!isValidDate(birthday)) return false;

        // 3. 校验前17位是否全是数字
        String body = id.substring(0, 17);
        if (!body.matches("\\d+")) return false;

        // 4. 校验码校验（最后一位）
        char expectedCheckCode = calculateCheckCode(body);
        char actualCheckCode = Character.toUpperCase(id.charAt(17)); // 可能是 x，要大写处理

        return expectedCheckCode == actualCheckCode;
    }

    /**
     * 判断一个日期字符串是否为合法的年月日（yyyyMMdd）
     *
     * @param dateStr 日期字符串（8位）
     * @return 合法返回 true，否则返回 false
     */
    private static boolean isValidDate(String dateStr) {
        try {
            java.time.LocalDate.parse(
                    dateStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据身份证前17位数字，计算校验位
     *
     * @param body 身份证号前17位数字
     * @return 应该出现的第18位校验码字符
     */
    private static char calculateCheckCode(String body) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            // 每位数字 * 对应权重
            sum += Character.getNumericValue(body.charAt(i)) * WEIGHT[i];
        }
        int mod = sum % 11; // 取模
        return CHECK_CODE[mod];
    }

    /**
     * 提取身份证中的性别信息
     *
     * @param id 身份证号
     * @return "男" 或 "女"，若非法返回 "未知"
     */
    public static String getGender(String id) {
        if (!isValid(id)) return "未知";
        int genderCode = Character.getNumericValue(id.charAt(16));
        return (genderCode % 2 == 0) ? "女" : "男";
    }

    /**
     * 提取身份证中的出生日期
     *
     * @param id 身份证号
     * @return 出生日期字符串（格式：yyyy-MM-dd），若非法返回 "未知"
     */
    public static String getBirthdate(String id) {
        if (!isValid(id)) return "未知";
        return id.substring(6, 10) + "-" +
                id.substring(10, 12) + "-" +
                id.substring(12, 14);
    }
}
