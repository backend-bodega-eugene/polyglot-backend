package com.eugene.goalhub.match.service;

import dto.AppMatchMarketLeagueResponse;
import dto.AppMatchMarketQueryRequest;
import dto.PageResponse;

/**
 * App 赛事玩法赔率聚合查询服务。
 */
public interface AppMatchMarketService {

    /**
     * 分页查询今日赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    PageResponse<AppMatchMarketLeagueResponse> pageToday(
            AppMatchMarketQueryRequest request);

    /**
     * 分页查询滚球赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    PageResponse<AppMatchMarketLeagueResponse> pageLive(
            AppMatchMarketQueryRequest request);

    /**
     * 分页查询早盘赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    PageResponse<AppMatchMarketLeagueResponse> pageEarly(
            AppMatchMarketQueryRequest request);

    /**
     * 分页查询串关赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    PageResponse<AppMatchMarketLeagueResponse> pageParlay(
            AppMatchMarketQueryRequest request);
}
