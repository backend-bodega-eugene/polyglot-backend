package com.eugene.goalhub.boot.logs.service;

/**
 * GoalHub 日志写入服务。
 *
 * <p>统一封装业务日志、系统日志和错误日志写入能力，当前实现用于将日志持久化到 MongoDB。</p>
 */
public interface GoalhubLogService {

    /**
     * 写入业务日志。
     *
     * <p>适用于记录后台或用户触发的业务变更，例如新增、更新、删除、审核、冻结和结算等操作。</p>
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
     * <p>适用于记录系统级事件，例如应用启动、定时任务执行、配置刷新和内部服务状态变化等。</p>
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
     * <p>适用于记录需要排查的异常信息，包含异常类型、消息和堆栈内容。</p>
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
