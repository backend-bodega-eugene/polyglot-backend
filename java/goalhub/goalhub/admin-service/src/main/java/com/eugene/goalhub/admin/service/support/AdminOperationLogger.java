package com.eugene.goalhub.admin.service.support;

import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 后台操作日志工具。
 *
 * <p>统一从当前请求头读取管理员身份，并调用公共日志服务写入 MongoDB。</p>
 */
@Component
public class AdminOperationLogger {

    /**
     * 当前管理员 ID 请求头。
     */
    private static final String ADMIN_ID_HEADER = "X-Admin-Id";

    /**
     * 当前管理员用户名请求头。
     */
    private static final String ADMIN_USERNAME_HEADER = "X-Admin-Username";

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建后台操作日志工具。
     *
     * @param goalhubLogService 日志写入服务
     */
    public AdminOperationLogger(GoalhubLogService goalhubLogService) {
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 写入当前管理员的业务日志。
     *
     * @param moduleName 业务模块名称
     * @param event      业务事件名称
     * @param content    业务日志内容
     */
    public void bizLog(String moduleName, String event, String content) {
        Operator operator = currentOperator();
        goalhubLogService.bizLog(
                moduleName,
                event,
                operator.operatorId(),
                operator.operatorName(),
                content
        );
    }

    /**
     * 使用指定操作人写入业务日志。
     *
     * @param moduleName   业务模块名称
     * @param event        业务事件名称
     * @param operatorId   操作人 ID
     * @param operatorName 操作人名称
     * @param content      业务日志内容
     */
    public void bizLog(
            String moduleName,
            String event,
            Long operatorId,
            String operatorName,
            String content
    ) {
        goalhubLogService.bizLog(moduleName, event, operatorId, operatorName, content);
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
     * 写入错误日志。
     *
     * @param moduleName 错误所属模块名称
     * @param event      错误事件名称
     * @param throwable  异常对象
     */
    public void errLog(String moduleName, String event, Throwable throwable) {
        goalhubLogService.errLog(moduleName, event, throwable);
    }

    /**
     * 读取当前请求中的管理员身份。
     *
     * @return 当前操作人
     */
    private Operator currentOperator() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return new Operator(null, null);
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        return new Operator(
                parseLong(request.getHeader(ADMIN_ID_HEADER)),
                request.getHeader(ADMIN_USERNAME_HEADER)
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
     * 后台操作人。
     *
     * @param operatorId   操作人 ID
     * @param operatorName 操作人名称
     */
    private record Operator(Long operatorId, String operatorName) {
    }
}
