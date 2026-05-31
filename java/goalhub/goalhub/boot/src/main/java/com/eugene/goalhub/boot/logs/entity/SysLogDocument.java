package com.eugene.goalhub.boot.logs.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 系统日志 MongoDB 文档。
 */
@Schema(description = "系统日志文档")
@Data
@Document("sys_logs")
public class SysLogDocument {

    /**
     * 文档 ID。
     */
    @Schema(description = "文档 ID", example = "6659f0d3b13c2f4d18f8a002")
    @Id
    private String id;

    /**
     * 产生日志的服务名称。
     */
    @Schema(description = "产生日志的服务名称", example = "admin-service")
    private String serviceName;

    /**
     * 系统模块名称。
     */
    @Schema(description = "系统模块名称", example = "系统启动")
    private String moduleName;

    /**
     * 系统事件名称。
     */
    @Schema(description = "系统事件名称", example = "APPLICATION_STARTED")
    private String event;

    /**
     * 系统日志内容。
     */
    @Schema(description = "系统日志内容", example = "服务启动成功")
    private String content;

    /**
     * 日志创建时间。
     */
    @Schema(description = "日志创建时间", example = "2026-05-30T12:00:00")
    private LocalDateTime createdAt;
}
