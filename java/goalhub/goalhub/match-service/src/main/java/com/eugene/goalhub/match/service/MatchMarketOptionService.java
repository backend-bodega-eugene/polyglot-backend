package com.eugene.goalhub.match.service;

import dto.*;

/**
 * 比赛投注选项管理服务。
 */
public interface MatchMarketOptionService {

    /**
     * 分页查询比赛投注选项。
     *
     * @param request 比赛投注选项分页查询条件
     * @return 比赛投注选项分页数据
     */
    PageResponse<MatchMarketOptionResponse> page(
            MatchMarketOptionPageRequest request);

    /**
     * 新增比赛投注选项。
     *
     * @param request 比赛投注选项新增参数
     */
    void add(
            AddMatchMarketOptionRequest request);

    /**
     * 更新比赛投注选项。
     *
     * @param request 比赛投注选项更新参数
     */
    void update(
            UpdateMatchMarketOptionRequest request);

    /**
     * 删除比赛投注选项。
     *
     * @param request 比赛投注选项删除参数
     */
    void delete(
            DeleteMatchMarketOptionRequest request);
}
