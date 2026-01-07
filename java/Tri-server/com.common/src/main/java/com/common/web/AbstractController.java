//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.web;

import com.common.security.DesDecrypter;
import com.common.security.DesEncrypter;
import com.common.util.BeanCoper;
import com.common.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class AbstractController implements Serializable {
    @Autowired(required = false)
    protected HttpServletRequest request;
    //登录用户id
    public static String LOGIN_USER_ID = "USER_ID";

    @SneakyThrows
    protected String getDomain() {
        URL url = new URL(request.getRequestURL().toString());
        return url.getHost();
    }

    protected HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }


    /**
     * 获取加密的cookie
     *
     * @param cookieKey
     * @param encodeKey
     * @return
     */
    protected String getCookie(String cookieKey, String encodeKey) {
        String cookiesValue = getCookieValue(cookieKey);
        return DesDecrypter.decryptString(cookiesValue, encodeKey);
    }

    /**
     * 获取cookie
     *
     * @param key
     * @return
     */
    protected String getCookieValue(String key) {
        Cookie[] cookies = this.getRequest().getCookies();
        if (cookies == null) {
            return null;
        }
        var option = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(key)).findFirst();
        if (option.isPresent()) {
            return option.get().getValue();
        }
        return null;
    }

    /**
     * 设置cookie
     *
     * @param name
     * @param value
     */
    protected void putCookie(String name, String value, HttpServletResponse response) {
        String domain = getDomain();
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(1800);//过期时间：0:不记录cookies,-1:会话级别(默认)，关闭浏览器失效，其他单位为秒（s）
        response.addCookie(cookie);
    }

    /**
     * 设置cookie
     *
     * @param name
     * @param value
     * @param encodeKey
     */
    protected void putCookie(String name, String value, String encodeKey, HttpServletResponse response) {
        String domain = StringUtils.getDomain(getRequest().getRequestURL().toString());
        value = DesEncrypter.cryptString(value, encodeKey);
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(domain);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 获取token
     *
     * @return
     */
    protected String getToken() {
        String token = request.getHeader("token");
        if (StringUtils.isNotBlank(token)) {
            return token;
        }
        var cookies = request.getCookies();
        if (cookies != null) {
            var cookie = Arrays.stream(request.getCookies())
                    .filter(item -> item.getName().equals("token"))
                    .findFirst();
            if (cookie.isPresent()) {
                return cookie.get().getValue();
            }
        }
        return null;
    }

    /**
     * 对象转换
     *
     * @param typeClass 目标类型
     * @param source    源类型
     * @param <T>
     * @return
     */
    protected <T> T clone(Class<T> typeClass, Object source) {
        return BeanCoper.copyProperties(typeClass, source);
    }

    /**
     * list数据转换
     *
     * @param typeClass  目标类型
     * @param sourcepage 源类型
     * @param <T>
     * @return
     */
    protected <T> List<T> clone(Class<T> typeClass, List<?> sourcepage) {
        return BeanCoper.copyList(typeClass, sourcepage);
    }

    /**
     * 分页数据转换
     *
     * @param typeClass  目标类型
     * @param sourcepage 源类型
     * @param <T>
     * @return
     */
    protected <T> Page<T> clone(Class<T> typeClass, Page<?> sourcepage) {
        return BeanCoper.copyPage(typeClass, sourcepage);
    }

}
