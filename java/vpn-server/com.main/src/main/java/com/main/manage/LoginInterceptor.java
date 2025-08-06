package com.main.manage;

import com.common.cookie.LoginContext;
import com.common.exception.BizException;
import com.main.Entity.VpnAdmin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author
 * @create
 * @desc 登录状态拦截器
 **/
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    final long logintimeminutes = 3 * 24 * 60;//3天超时

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //   log.info("==========登录状态拦截");
        LoginContext<VpnAdmin> logininfo = LoginContext.getTicket();
        if (null == logininfo) {
            response.setStatus(401);
            throw new BizException(401, "你还没有登录");
            // return false;
        } else {
            Object currentcount = request.getSession().getAttribute(logininfo.getVpnAdmin().getAdminName());
            if (currentcount == null) {
                response.setStatus(401);
                throw new BizException(401, "登录状态异常");
                // return false;

            }
            String cu = String.valueOf(currentcount);
            if (!AdminLoginInfo.GetAdminLoginInfo(logininfo.getVpnAdmin().getAdminName()).equals(cu)) {
                LoginContext.RemoveTicket();
                request.getSession().removeAttribute(logininfo.getVpnAdmin().getAdminName());
                response.setStatus(401);
                throw new BizException(401, "用户在其他地方登录");

            }
            LocalDateTime before = logininfo.getCreateTime();
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(before, now);
            long minutes = duration.toMinutes();
            if (minutes > logintimeminutes) {
                log.info("==========登录状态拦截");
                response.setStatus(401);
                throw new BizException(401, "登录超时,请重新登录");
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

    }
}