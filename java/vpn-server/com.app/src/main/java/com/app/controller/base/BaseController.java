package com.app.controller.base;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.app.manage.ReturnCard;
import com.common.exception.BizException;
import com.common.web.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
    protected DeviceTypeEnum getDeviceType(HttpServletRequest request) {
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

    protected Long getSession() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var loginkey = servletRequestAttributes.getRequest().getHeader("userSession");
        if (null == loginkey) {
            throw new BizException(1, "请检查登录状态,id获取失败");
        }
        loginkey = ReturnCard.GetSingleton().DecodeSession(loginkey);

        String[] strarr = loginkey.split("_");
        if (strarr.length > 1) {
            log.info("获取登录id:"+strarr[0]);
            return Long.parseLong(strarr[0]);
        }
        throw new BizException(1, "请检查登录状态,id获取失败");

    }
}
