package com.eugene.goalhub.match.service;

import dto.AppMatchOddsResponse;

/**
 * 前端赛事赔率查询服务。
 *
 * <p>负责按赛事 ID 组装前端展示用的赛事玩法和赔率信息。</p>
 */
public interface AppMatchOddsService {

    /**
     * 查询指定赛事的赔率信息。
     *
     * @param matchId 赛事 ID
     * @return 赛事赔率聚合响应
     */
    AppMatchOddsResponse getMatchOdds(
            Long matchId);
}
