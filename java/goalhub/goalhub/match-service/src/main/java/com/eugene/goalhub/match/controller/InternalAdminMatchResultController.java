package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.MatchResultService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 内部后台比赛结果管理接口。
 */
@Tag(name = "内部后台比赛结果管理", description = "内部后台比赛结果分页查询、保存和审核接口")
@RestController
@RequestMapping("/internal/admin/matchresult")
public class InternalAdminMatchResultController {

    private final MatchResultService matchResultService;

    public InternalAdminMatchResultController(
            MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
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
                matchResultService.page(request)
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

        matchResultService.save(request);

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

        matchResultService.approve(request);

        return Result.success();
    }
}
