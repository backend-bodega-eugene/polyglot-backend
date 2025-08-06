package com.app.manage;

import com.app.Entity.VpnMember;
import com.common.cookie.LoginContext;
import com.common.exception.BizException;
import com.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author
 * @create
 * @desc 登录状态拦截器
 **/
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String isVisited = request.getHeader("isVisiter");
        if (StringUtils.isNotBlank(isVisited)) {
            if (isVisited.equals("1") && request.getRequestURI().contains("vpnpackage")) {
                return true;
            }
        }
        String loginkey = request.getHeader("userSession");
        if (!StringUtils.isNotBlank(loginkey)) {
            response.setHeader("loginstatus", "no login");
            log.info("==========登录状态拦截 没有找到usersession");
            throw new BizException(1, "你没有登录");
        }
        loginkey = ReturnCard.GetSingleton().DecodeSession(loginkey);
        String[] strarr = loginkey.split("_");
        if (strarr.length > 1) {

            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime ldt = LocalDateTime.parse(strarr[1], df);
            if (LocalDateTime.now().isBefore(ldt)) {
                return true;
            } else {
                response.setHeader("loginstatus", "login timeout");
                log.info("==========登录状态拦截 :登录已经过期");
                throw new BizException(1, "登录已经过期");
                //return false;
            }
        }
        response.setHeader("loginstatus", "unknown error");
        log.info("==========登录状态拦截 其他错误");
        throw new BizException(1, "其他错误");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

    }
}