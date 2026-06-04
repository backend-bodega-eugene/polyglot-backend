package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.AppMatchResultService;
import dto.AppMatchResultPageRequest;
import dto.AppMatchResultResponse;
import dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "前端赛事赛果接口", description = "前端App赛事赛果查询接口")
@RestController
@RequestMapping("/soccer/matches")
public class SoccerMatchResultController {

    private final AppMatchResultService appMatchResultService;

    public SoccerMatchResultController(
            AppMatchResultService appMatchResultService) {
        this.appMatchResultService = appMatchResultService;
    }

    @Operation(summary = "分页查询赛事赛果")
    @PostMapping("/results/page")
    public Result<PageResponse<AppMatchResultResponse>> pageResult(
            @RequestBody AppMatchResultPageRequest request) {

        return Result.success(
                appMatchResultService.pageResult(request)
        );
    }
}