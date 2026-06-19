package com.eugene.goalhub.admin.service;

import dto.*;

import java.util.List;

/**
 * 后台冠军赔率管理服务。
 */
public interface AdminChampionMarketOddsService {

    /**
     * 分页查询冠军赔率配置列表。
     *
     * @param request 冠军赔率分页查询参数
     * @return 冠军赔率分页结果
     */
    PageResponse<ChampionMarketOddsResponse> page(
            ChampionMarketOddsPageRequest request);

    /**
     * 查询指定联赛下出现过的球队。
     *
     * @param request 联赛球队查询参数
     * @return 联赛球队列表
     */
    List<ChampionLeagueTeamResponse> leagueTeams(
            ChampionLeagueTeamRequest request);

    /**
     * 新增冠军赔率配置。
     *
     * @param request 新增冠军赔率参数
     */
    void add(
            AddChampionMarketOddsRequest request);

    /**
     * 更新冠军赔率配置。
     *
     * @param request 更新冠军赔率参数
     */
    void update(
            UpdateChampionMarketOddsRequest request);

    /**
     * 删除指定冠军赔率配置。
     *
     * @param request 删除冠军赔率参数
     */
    void delete(
            DeleteChampionMarketOddsRequest request);
}
