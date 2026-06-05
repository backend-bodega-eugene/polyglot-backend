package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志分页查询请求。
 *
 * <p>用于按服务、模块、事件、操作人和创建时间范围分页查询日志。</p>
 */
@Schema(description = "日志分页查询请求")
@Data
public class LogQueryRequest {

    /**
     * 页码。
     */
    @Schema(description = "页码", example = "1")
    private Integer pageIndex = 1;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    /**
     * 服务名称筛选条件。
     */
    @Schema(description = "服务名称筛选条件", example = "admin-service")
    private String serviceName;

    /**
     * 模块名称筛选条件。
     */
    @Schema(description = "模块名称筛选条件", example = "用户管理")
    private String moduleName;

    /**
     * 事件名称筛选条件。
     */
    @Schema(description = "事件名称筛选条件", example = "CREATE_USER")
    private String event;

    /**
     * 操作人名称筛选条件。
     */
    @Schema(description = "操作人名称筛选条件", example = "admin")
    private String operatorName;

    /**
     * 创建时间起始范围。
     */
    @Schema(description = "创建时间起始范围", example = "2026-05-30T00:00:00")
    private LocalDateTime createdAtStart;

    /**
     * 创建时间结束范围。
     */
    @Schema(description = "创建时间结束范围", example = "2026-05-30T23:59:59")
    private LocalDateTime createdAtEnd;
}
