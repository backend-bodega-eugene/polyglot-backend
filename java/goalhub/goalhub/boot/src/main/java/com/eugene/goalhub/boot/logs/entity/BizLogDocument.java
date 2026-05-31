package com.eugene.goalhub.boot.logs.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 业务日志 MongoDB 文档。
 */
@Schema(description = "业务日志文档")
@Data
@Document("biz_logs")
public class BizLogDocument {

    /**
     * 文档 ID。
     */
    @Schema(description = "文档 ID", example = "6659f0d3b13c2f4d18f8a001")
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
     * 业务事件名称。
     */
    @Schema(description = "业务事件名称", example = "CREATE_USER")
    private String event;

    /**
     * 操作人 ID。
     */
    @Schema(description = "操作人 ID", example = "1")
    private Long operatorId;

    /**
     * 操作人名称。
     */
    @Schema(description = "操作人名称", example = "admin")
    private String operatorName;

    /**
     * 链路追踪 ID。
     */
    @Schema(description = "链路追踪 ID", example = "trace-123456")
    private String traceId;

    /**
     * 业务日志内容。
     */
    @Schema(description = "业务日志内容", example = "创建用户成功")
    private String content;

    /**
     * 日志创建时间。
     */
    @Schema(description = "日志创建时间", example = "2026-05-30T12:00:00")
    private LocalDateTime createdAt;
}
