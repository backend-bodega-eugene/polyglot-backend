package com.common.util;

import com.common.exception.ApplicationException;

import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils extends org.springframework.util.StringUtils {
    /**
     * 重写防止修改过多的业务
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(CharSequence str) {
        return hasLength(str);
    }

    /**
     * 重写防止修改过多的业务
     *
     * @param str
     * @return
     */
    public static boolean isNotBlank(CharSequence str) {
        return hasText(str);
    }

    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^1[123456789]\\d{9}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static boolean isEmail(String email) {
        Pattern pattern = Pattern.compile("^\\w+([-.]\\w+)*@\\w+([-]\\w+)*\\.(\\w+([-]\\w+)*\\.)*[a-z]{2,3}$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String getUUID() {
        return replace(UUID.randomUUID().toString(), "-", "");
    }

    /**
     * 解析域名
     *
     * @param url
     * @return
     */
    public static String getDomain(String url) {
        int i = url.indexOf("//");
        if (i == -1) {
            throw new ApplicationException("url.format.error");
        }
        url = url.substring(i + 2);
        i = url.indexOf("/");
        if (i == -1) {
            i = url.indexOf("?");
            url = url.substring(0, i);
        } else {
            url = url.substring(0, i);
        }
        i = url.lastIndexOf(".");
        if (i == -1) {
            throw new ApplicationException("url.format.error");
        }
        String temp = url.substring(0, i);
        i = temp.lastIndexOf(".", i);
        url = url.substring(i + 1);
        i = url.indexOf(":");
        if (i != -1) {
            url = url.substring(0, i);
        }
        return url;
    }

    /**
     * 生成6位随机验证码
     *
     * @return
     */
    public static String randomSixCode() {
        return String.valueOf(new Random().nextInt(899999) + 100000);
    }

}
