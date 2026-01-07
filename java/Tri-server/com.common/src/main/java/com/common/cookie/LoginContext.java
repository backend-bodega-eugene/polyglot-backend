//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.cookie;

import com.common.exception.ApplicationException;
import com.common.security.DesDecrypter;
import com.common.security.DesEncrypter;
import com.common.util.DateUtils;
import com.common.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Data
public class LoginContext<T> implements Serializable {
    private String loginName;
    private LocalDateTime createTime;
    private long expires;
    private T vpnAdmin;
    private int currentCount;

    public void setTimeout(long timeout) {
        this.expires = this.createTime.toInstant(ZoneOffset.of("+8")).toEpochMilli() + timeout;
    }

    public void SetCurrentCounts(int _currentCount) {
        currentCount = _currentCount;

    }

    public String toString() {
        if (!StringUtils.isNotBlank(this.loginName)) {
            throw new ApplicationException("loginName can.t be blank");
        } else if (this.createTime == null) {
            throw new ApplicationException("loginTime can\'t be null");
        } else {
            StringBuilder content = new StringBuilder();
            content.append(this.loginName).append(",");
            content.append(DateUtils.localDateFormat(this.createTime)).append(",");
            content.append(this.expires).append(",");
            return content.toString();
        }
    }

    public static LoginContext getTicket() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LoginContext attribute = (LoginContext) request.getSession().getAttribute("ticket");
        return attribute;
    }

    public static void setTicket(LoginContext ticket) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.getSession().setAttribute("ticket", ticket);
    }

    public static void RemoveTicket() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.getSession().removeAttribute("ticket");
    }

    public static LoginContext getTicket(String content) {
        if (!StringUtils.isNotBlank(content)) {
            return null;
        } else {
            String[] split = content.split(",");
            LoginContext info = new LoginContext();
            info.setLoginName(split[0]);

            try {
                info.setCreateTime(LocalDateTime.parse(split[1], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                return info;
            } catch (Exception e) {
                log.error("getToken  error ", e);
                throw new ApplicationException("getToken error", e);
            }
        }
    }

    public static LoginContext getTicket(String encrypted, String key) {
        String content = deCodeToken(encrypted, key);
        return getTicket(content);
    }

    public static String enCodeToken(LoginContext info, String key) {
        String encrypted = null;

        try {
            encrypted = DesEncrypter.cryptString(info.toString(), key);
            return encrypted;
        } catch (Exception var4) {
            log.error("enCodeToken error", var4);
            throw new ApplicationException("enCodeToken error", var4);
        }
    }

    public static String deCodeToken(String encrypted, String key) {
        String plain = null;

        try {
            plain = DesDecrypter.decryptString(encrypted, key);
            return plain;
        } catch (Exception var4) {
            log.error("deCodeToken error", var4);
            throw new ApplicationException("deCodeToken error", var4);
        }
    }

}
