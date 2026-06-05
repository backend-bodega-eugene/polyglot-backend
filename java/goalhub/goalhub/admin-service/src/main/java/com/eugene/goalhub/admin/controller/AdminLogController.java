package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.boot.logs.entity.BizLogDocument;
import com.eugene.goalhub.boot.logs.entity.ErrLogDocument;
import com.eugene.goalhub.boot.logs.entity.SysLogDocument;
import com.eugene.goalhub.boot.logs.service.GoalhubLogQueryService;
import dto.LogQueryRequest;
import dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

/**
 * 后台日志查询接口。
 *
 * <p>提供业务日志、系统日志和错误日志的只读分页查询能力。</p>
 */
@Tag(name = "后台日志管理", description = "后台业务日志、系统日志和错误日志查询接口")
@RestController
@RequestMapping("/admin/logs")
public class AdminLogController {

    /**
     * 日志查询服务。
     */
    private final GoalhubLogQueryService goalhubLogQueryService;

    /**
     * 创建后台日志查询接口实例。
     *
     * @param goalhubLogQueryService 日志查询服务
     */
    public AdminLogController(GoalhubLogQueryService goalhubLogQueryService) {
        this.goalhubLogQueryService = goalhubLogQueryService;
    }

    /**
     * 分页查询业务日志。
     *
     * @param request 日志分页查询条件
     * @return 业务日志分页数据
     */
    @Operation(summary = "分页查询业务日志", description = "根据分页条件和筛选条件查询业务日志列表。")
    @PostMapping("/biz/page")
    public Result<PageResponse<BizLogDocument>> queryBizLogs(
            @Parameter(description = "日志分页查询参数", required = true)
            @RequestBody LogQueryRequest request) {
        return Result.success(goalhubLogQueryService.queryBizLogs(request));
    }

    /**
     * 分页查询系统日志。
     *
     * @param request 日志分页查询条件
     * @return 系统日志分页数据
     */
    @Operation(summary = "分页查询系统日志", description = "根据分页条件和筛选条件查询系统日志列表。")
    @PostMapping("/sys/page")
    public Result<PageResponse<SysLogDocument>> querySysLogs(
            @Parameter(description = "日志分页查询参数", required = true)
            @RequestBody LogQueryRequest request) {
        return Result.success(goalhubLogQueryService.querySysLogs(request));
    }

    /**
     * 分页查询错误日志。
     *
     * @param request 日志分页查询条件
     * @return 错误日志分页数据
     */
    @Operation(summary = "分页查询错误日志", description = "根据分页条件和筛选条件查询错误日志列表。")
    @PostMapping("/err/page")
    public Result<PageResponse<ErrLogDocument>> queryErrLogs(
            @Parameter(description = "日志分页查询参数", required = true)
            @RequestBody LogQueryRequest request) {
        return Result.success(goalhubLogQueryService.queryErrLogs(request));
    }
}
