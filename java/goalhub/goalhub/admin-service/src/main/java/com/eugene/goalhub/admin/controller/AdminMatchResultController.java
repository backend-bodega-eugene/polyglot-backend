package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminMatchResultService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台比赛结果管理接口。
 *
 * <p>提供比赛结果分页查询、结果保存和审核入口，用于后台维护赛果数据。</p>
 */
@Tag(name = "后台比赛结果管理", description = "后台比赛结果保存、审核和查询接口")
@RestController
@RequestMapping("/admin/matchresult")
public class AdminMatchResultController {

    /**
     * 后台比赛结果服务。
     */
    private final AdminMatchResultService
            adminMatchResultService;

    /**
     * 创建后台比赛结果管理接口实例。
     *
     * @param adminMatchResultService 后台比赛结果服务
     */
    public AdminMatchResultController(
            AdminMatchResultService adminMatchResultService) {
        this.adminMatchResultService =
                adminMatchResultService;
    }

    /**
     * 分页查询比赛结果。
     *
     * @param request 比赛结果分页查询条件
     * @return 比赛结果分页数据
     */
    @Operation(summary = "分页查询比赛结果", description = "根据分页条件和筛选条件查询比赛结果列表。")
    @PostMapping("/page")
    public Result<PageResponse<AdminMatchResultResponse>> page(
            @Parameter(description = "比赛结果分页查询参数", required = true)
            @RequestBody AdminMatchResultPageRequest request) {

        return Result.success(
                adminMatchResultService.page(request)
        );
    }

    /**
     * 保存比赛结果。
     *
     * @param request 比赛结果保存参数
     * @return 空结果
     */
    @Operation(summary = "保存比赛结果", description = "保存指定比赛的结果信息。")
    @PostMapping("/save")
    public Result<Void> save(
            @Parameter(description = "比赛结果保存参数", required = true)
            @RequestBody SaveMatchResultRequest request) {

        adminMatchResultService.save(request);

        return Result.success();
    }

    /**
     * 审核比赛结果。
     *
     * @param request 比赛结果审核参数
     * @return 空结果
     */
    @Operation(summary = "审核比赛结果", description = "审核指定比赛的结果信息。")
    @PostMapping("/approve")
    public Result<Void> approve(
            @Parameter(description = "比赛结果审核参数", required = true)
            @RequestBody ApproveMatchResultRequest request) {

        adminMatchResultService.approve(request);

        return Result.success();
    }
}
