package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.AppMatchMarketService;
import dto.AppMatchMarketLeagueResponse;
import dto.AppMatchMarketQueryRequest;
import dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端赛事玩法赔率聚合接口。
 *
 * <p>提供今日、滚球、早盘和串关等赛事玩法赔率聚合查询能力。</p>
 */
@Tag(name = "前端赛事玩法赔率聚合接口", description = "今日、滚球、早盘、串关赛事玩法赔率聚合查询")
@RestController
@RequestMapping("/soccer/matchmarkets")
public class SoccerMatchMarketController {

    /**
     * App 赛事玩法赔率聚合查询服务。
     */
    private final AppMatchMarketService appMatchMarketService;

    /**
     * 创建前端赛事玩法赔率聚合接口实例。
     *
     * @param appMatchMarketService App 赛事玩法赔率聚合查询服务
     */
    public SoccerMatchMarketController(
            AppMatchMarketService appMatchMarketService) {
        this.appMatchMarketService = appMatchMarketService;
    }

    /**
     * 查询今日赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    @Operation(summary = "今日赛事玩法赔率", description = "查询今日未结束且有可下注赔率的赛事。")
    @PostMapping("/today")
    public Result<PageResponse<AppMatchMarketLeagueResponse>> today(
            @Parameter(description = "赛事玩法赔率聚合查询参数", required = true)
            @RequestBody AppMatchMarketQueryRequest request) {

        return Result.success(
                appMatchMarketService.pageToday(request)
        );
    }

    /**
     * 查询滚球赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    @Operation(summary = "滚球赛事玩法赔率", description = "查询状态为 LIVE 且有可下注赔率的赛事。")
    @PostMapping("/live")
    public Result<PageResponse<AppMatchMarketLeagueResponse>> live(
            @Parameter(description = "赛事玩法赔率聚合查询参数", required = true)
            @RequestBody AppMatchMarketQueryRequest request) {

        return Result.success(
                appMatchMarketService.pageLive(request)
        );
    }

    /**
     * 查询早盘赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    @Operation(summary = "早盘赛事玩法赔率", description = "查询明天和后天未开始且有可下注赔率的赛事。")
    @PostMapping("/early")
    public Result<PageResponse<AppMatchMarketLeagueResponse>> early(
            @Parameter(description = "赛事玩法赔率聚合查询参数", required = true)
            @RequestBody AppMatchMarketQueryRequest request) {

        return Result.success(
                appMatchMarketService.pageEarly(request)
        );
    }

    /**
     * 查询串关赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    @Operation(summary = "串关赛事玩法赔率", description = "查询未开始或进行中且有可下注赔率的赛事，不包含冠军玩法。")
    @PostMapping("/parlay")
    public Result<PageResponse<AppMatchMarketLeagueResponse>> parlay(
            @Parameter(description = "赛事玩法赔率聚合查询参数", required = true)
            @RequestBody AppMatchMarketQueryRequest request) {

        return Result.success(
                appMatchMarketService.pageParlay(request)
        );
    }
}
