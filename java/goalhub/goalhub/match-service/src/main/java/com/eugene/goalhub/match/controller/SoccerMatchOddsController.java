package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.AppMatchOddsService;
import dto.AppMatchOddsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "前端赛事赔率接口", description = "前端App赛事赔率查询接口")
@RestController
@RequestMapping("/soccer/matches")
public class SoccerMatchOddsController {

    private final AppMatchOddsService appMatchOddsService;

    public SoccerMatchOddsController(
            AppMatchOddsService appMatchOddsService) {
        this.appMatchOddsService = appMatchOddsService;
    }

    @Operation(summary = "查询赛事赔率")
    @GetMapping("/{matchId}/odds")
    public Result<AppMatchOddsResponse> getMatchOdds(
            @PathVariable("matchId") Long matchId) {

        return Result.success(
                appMatchOddsService.getMatchOdds(matchId)
        );
    }
}