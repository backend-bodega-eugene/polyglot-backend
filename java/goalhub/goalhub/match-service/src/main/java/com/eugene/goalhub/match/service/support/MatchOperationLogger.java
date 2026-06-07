package com.eugene.goalhub.match.service.support;

import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 比赛服务操作日志工具。
 *
 * <p>统一读取当前请求中的后台管理员或前端用户身份，并调用公共日志服务写入 MongoDB。</p>
 */
@Component
public class MatchOperationLogger {

    /**
     * 后台管理员 ID 请求头。
     */
    private static final String ADMIN_ID_HEADER = "X-Admin-Id";

    /**
     * 后台管理员用户名请求头。
     */
    private static final String ADMIN_USERNAME_HEADER = "X-Admin-Username";

    /**
     * 前端用户 ID 请求头。
     */
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 前端用户名请求头。
     */
    private static final String USERNAME_HEADER = "X-Username";

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建比赛服务操作日志工具。
     *
     * @param goalhubLogService 日志写入服务
     */
    public MatchOperationLogger(GoalhubLogService goalhubLogService) {
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 写入当前后台管理员的业务日志。
     *
     * @param moduleName 业务模块名称
     * @param event      业务事件名称
     * @param content    业务日志内容
     */
    public void adminBizLog(String moduleName, String event, String content) {
        Operator operator = currentOperator(ADMIN_ID_HEADER, ADMIN_USERNAME_HEADER);
        goalhubLogService.bizLog(
                moduleName,
                event,
                operator.operatorId(),
                operator.operatorName(),
                content
        );
    }

    /**
     * 写入当前前端用户的业务日志。
     *
     * @param moduleName 业务模块名称
     * @param event      业务事件名称
     * @param content    业务日志内容
     */
    public void userBizLog(String moduleName, String event, String content) {
        Operator operator = currentOperator(USER_ID_HEADER, USERNAME_HEADER);
        goalhubLogService.bizLog(
                moduleName,
                event,
                operator.operatorId(),
                operator.operatorName(),
                content
        );
    }

    /**
     * 写入系统日志。
     *
     * @param moduleName 系统模块名称
     * @param event      系统事件名称
     * @param content    系统日志内容
     */
    public void sysLog(String moduleName, String event, String content) {
        goalhubLogService.sysLog(moduleName, event, content);
    }

    /**
     * 读取指定请求头中的操作人。
     *
     * @param idHeader   操作人 ID 请求头
     * @param nameHeader 操作人名称请求头
     * @return 当前操作人
     */
    private Operator currentOperator(String idHeader, String nameHeader) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return new Operator(null, null);
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        return new Operator(
                parseLong(request.getHeader(idHeader)),
                request.getHeader(nameHeader)
        );
    }

    /**
     * 将字符串解析为 Long。
     *
     * @param value 字符串值
     * @return 解析失败时返回 null
     */
    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 操作人。
     *
     * @param operatorId   操作人 ID
     * @param operatorName 操作人名称
     */
    private record Operator(Long operatorId, String operatorName) {
    }
}
