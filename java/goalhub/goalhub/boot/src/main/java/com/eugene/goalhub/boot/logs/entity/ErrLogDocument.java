package com.eugene.goalhub.boot.logs.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 错误日志 MongoDB 文档。
 */
@Schema(description = "错误日志文档")
@Data
@Document("err_logs")
public class ErrLogDocument {

    /**
     * 文档 ID。
     */
    @Schema(description = "文档 ID", example = "6659f0d3b13c2f4d18f8a003")
    @Id
    private String id;

    /**
     * 产生日志的服务名称。
     */
    @Schema(description = "产生日志的服务名称", example = "admin-service")
    private String serviceName;

    /**
     * 业务模块名称。
     */
    @Schema(description = "业务模块名称", example = "用户管理")
    private String moduleName;

    /**
     * 错误事件名称。
     */
    @Schema(description = "错误事件名称", example = "CREATE_USER_FAILED")
    private String event;

    /**
     * 异常类型全限定名。
     */
    @Schema(description = "异常类型全限定名", example = "java.lang.RuntimeException")
    private String exceptionType;

    /**
     * 异常消息。
     */
    @Schema(description = "异常消息", example = "用户创建失败")
    private String message;

    /**
     * 异常堆栈信息。
     */
    @Schema(description = "异常堆栈信息")
    private String stackTrace;

    /**
     * 链路追踪 ID。
     */
    @Schema(description = "链路追踪 ID", example = "trace-123456")
    private String traceId;

    /**
     * 日志创建时间。
     */
    @Schema(description = "日志创建时间", example = "2026-05-30T12:00:00")
    private LocalDateTime createdAt;
}
