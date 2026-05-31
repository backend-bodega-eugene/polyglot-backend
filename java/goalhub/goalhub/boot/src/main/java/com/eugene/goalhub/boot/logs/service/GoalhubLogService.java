package com.eugene.goalhub.boot.logs.service;

/**
 * GoalHub 日志写入服务。
 */
public interface GoalhubLogService {

    /**
     * 写入业务日志。
     *
     * @param moduleName   业务模块名称
     * @param event        业务事件名称
     * @param operatorId   操作人 ID
     * @param operatorName 操作人名称
     * @param content      业务日志内容
     */
    void bizLog(
            String moduleName,
            String event,
            Long operatorId,
            String operatorName,
            String content
    );

    /**
     * 写入系统日志。
     *
     * @param moduleName 系统模块名称
     * @param event      系统事件名称
     * @param content    系统日志内容
     */
    void sysLog(
            String moduleName,
            String event,
            String content
    );

    /**
     * 写入错误日志。
     *
     * @param moduleName 错误所属模块名称
     * @param event      错误事件名称
     * @param throwable  异常对象
     */
    void errLog(
            String moduleName,
            String event,
            Throwable throwable
    );
}
