package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.AppMatchResultService;
import dto.AppMatchResultPageRequest;
import dto.AppMatchResultResponse;
import dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端赛事赛果查询接口。
 *
 * <p>提供前端 App 分页查询赛事赛果的接口。</p>
 */
@Tag(name = "前端赛事赛果接口", description = "前端App赛事赛果查询接口")
@RestController
@RequestMapping("/soccer/matches")
public class SoccerMatchResultController {

    /**
     * 前端赛事赛果服务。
     */
    private final AppMatchResultService appMatchResultService;

    /**
     * 创建前端赛事赛果查询接口实例。
     *
     * @param appMatchResultService 前端赛事赛果服务
     */
    public SoccerMatchResultController(
            AppMatchResultService appMatchResultService) {
        this.appMatchResultService = appMatchResultService;
    }

    /**
     * 分页查询赛事赛果。
     *
     * @param request 赛事赛果分页查询参数
     * @return 赛事赛果分页数据
     */
    @Operation(summary = "分页查询赛事赛果", description = "根据分页条件查询前端展示用的赛事赛果列表。")
    @PostMapping("/results/page")
    public Result<PageResponse<AppMatchResultResponse>> pageResult(
            @Parameter(description = "赛事赛果分页查询参数", required = true)
            @Valid @RequestBody AppMatchResultPageRequest request) {

        return Result.success(
                appMatchResultService.pageResult(request)
        );
    }
}
