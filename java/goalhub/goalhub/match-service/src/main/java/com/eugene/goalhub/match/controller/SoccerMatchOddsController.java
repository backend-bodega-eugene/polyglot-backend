package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.AppMatchOddsService;
import dto.AppMatchOddsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端赛事赔率查询接口。
 *
 * <p>提供前端 App 查询指定赛事赔率信息的接口。</p>
 */
@Tag(name = "前端赛事赔率接口", description = "前端App赛事赔率查询接口")
@RestController
@RequestMapping("/soccer/matches")
public class SoccerMatchOddsController {

    /**
     * 前端赛事赔率服务。
     */
    private final AppMatchOddsService appMatchOddsService;

    /**
     * 创建前端赛事赔率查询接口实例。
     *
     * @param appMatchOddsService 前端赛事赔率服务
     */
    public SoccerMatchOddsController(
            AppMatchOddsService appMatchOddsService) {
        this.appMatchOddsService = appMatchOddsService;
    }

    /**
     * 查询指定赛事赔率。
     *
     * @param matchId 赛事 ID
     * @return 赛事赔率信息
     */
    @Operation(summary = "查询赛事赔率", description = "根据赛事 ID 查询前端展示用的赛事赔率信息。")
    @GetMapping("/{matchId}/odds")
    public Result<AppMatchOddsResponse> getMatchOdds(
            @Parameter(description = "赛事 ID", required = true)
            @PathVariable("matchId") Long matchId) {

        return Result.success(
                appMatchOddsService.getMatchOdds(matchId)
        );
    }
}
