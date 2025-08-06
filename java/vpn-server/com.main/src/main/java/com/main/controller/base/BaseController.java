package com.main.controller.base;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.common.web.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class BaseController extends AbstractController {
    protected enum DeviceTypeEnum {
        Android(0),
        Ios(1);
        private final int value;

        DeviceTypeEnum(int i) {
            this.value = i;
        }

        public int getValue() {
            return value;
        }
    }
   /* @Resource
    protected BizUserService bizUserService;

    protected Long getUserId() {
        return (Long) request.getAttribute(SessionConst.SESSION_USER_ID);
    }

    protected UserInfo getUser() {
        var userId = (Long) request.getAttribute(SessionConst.SESSION_USER_ID);
        if (userId == null) {
            throw new BizException("401", "登录已失效");
        }
        return bizUserService.getById(userId);
    }

    protected OrgInfo getOrgInfo() {
        return (OrgInfo) request.getAttribute(SessionConst.SESSION_ORG_INFO);
    }

    *//**
     * 验证码校验
     *
     * @param googleCode
     * @return
     *//*
    @Deprecated
    protected void checkVerifyCode(HttpServletRequest request, String googleCode) {
        String verifyCodeExpected = (String) request.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (googleCode == null || !googleCode.equals(verifyCodeExpected)) {
            throw new BizException("400", "验证码无效");
        }
    }

    */

    /**
     * 校验用户资金密码
     *
     * @param userInfo
     * @param fundPass
     * @param googleCode
     *//*
    protected void verifyUserFundPass(UserInfo userInfo, String fundPass, String googleCode) {
        if (userInfo.getIsDualAuth()) {
            if (!StringUtils.hasText(googleCode)) {
                log.warn("谷歌验证码为空");
                throw new BizException(ErrorCodeConst.GOOGLE_CODE_ERROR, "验证码错误");
            }
            Long code = null;
            try {
                code = Long.valueOf(googleCode);
            } catch (Exception e) {
                log.warn("谷歌验证码错误code={}", googleCode);
                throw new BizException(ErrorCodeConst.GOOGLE_CODE_ERROR, "验证码错误");
            }
            String secret = userInfo.getSecretKey();
            var mills = System.currentTimeMillis();
            var googleAuthenticator = new GoogleAuthenticator();
            googleAuthenticator.setWindowSize(5);
            boolean isOK = googleAuthenticator.check_code(secret, code, mills);
            if (!isOK) {
                throw new BizException(ErrorCodeConst.GOOGLE_CODE_ERROR, "验证码错误");
            }
        }
        if (!userInfo.getPayPassword().equals(fundPass)) {
            throw new BizException(ErrorCodeConst.FUND_PASS_ERROR, "资金密码错误");
        }
    }*/
    protected DeviceTypeEnum getDeviceType(HttpServletRequest request) {
        /*String agent = request.getHeader("User-Agent").toLowerCase();
        if (agent.indexOf("ipad") > -1 || agent.indexOf("iphone") > -1) {
            return DeviceTypeEnum.Ios;
        }
        return DeviceTypeEnum.Android;*/
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgentUtil.parse(agent);
        if (userAgent.getPlatform().isIos()) {
            return DeviceTypeEnum.Ios;
        }
        return DeviceTypeEnum.Android;
    }

    /**
     * 判断是否是手机端请求
     *
     * @param req
     * @return
     */
    public static boolean isMobile(HttpServletRequest req) {
        String uaStr = req.getHeader("User-Agent");
        printUserAgentInfo(uaStr);
        UserAgent ua = UserAgentUtil.parse(uaStr);
        return ua.isMobile();
    }

    /**
     * 打印终端信息
     *
     * @param uaStr
     */
    public static void printUserAgentInfo(String uaStr) {
        UserAgent ua = UserAgentUtil.parse(uaStr);
        if (ua == null) {
            log.info("ua is null");
            return;
        }
        log.info("User-Agent: " + uaStr);
        log.info("ua.getBrowser().toString(): " + ua.getBrowser().toString());
        log.info("ua.getVersion(): " + ua.getVersion());
        log.info("ua.getEngine().toString(): " + ua.getEngine().toString());
        log.info("ua.getEngineVersion(): " + ua.getEngineVersion());
        log.info("ua.getOs().toString(): " + ua.getOs().toString());
        log.info("ua.getPlatform().toString(): " + ua.getPlatform().toString());
    }

    /**
     * 获取ip地址
     *
     * @param request
     * @return
     */
    protected static String getIpAddress(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if (index != -1) {
                return XFor.substring(0, index);
            } else {
                return XFor;
            }
        }
        XFor = Xip;
        if (StringUtils.hasText(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            return XFor;
        }
        if (!StringUtils.hasText(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!StringUtils.hasText(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!StringUtils.hasText(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
    }
}
