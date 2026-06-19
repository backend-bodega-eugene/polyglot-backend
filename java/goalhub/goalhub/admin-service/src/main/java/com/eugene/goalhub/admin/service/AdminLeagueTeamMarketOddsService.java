package com.eugene.goalhub.admin.service;

import dto.*;

import java.util.List;

/**
 * 后台联盟球队玩法赔率管理服务。
 */
public interface AdminLeagueTeamMarketOddsService {

    /**
     * 分页查询联盟球队玩法赔率配置列表。
     *
     * @param request 联盟球队玩法赔率分页查询参数
     * @return 联盟球队玩法赔率分页结果
     */
    PageResponse<LeagueTeamMarketOddsResponse> page(
            LeagueTeamMarketOddsPageRequest request);

    /**
     * 新增联盟球队玩法赔率配置。
     *
     * @param request 新增联盟球队玩法赔率参数
     */
    void add(
            AddLeagueTeamMarketOddsRequest request);

    /**
     * 更新联盟球队玩法赔率配置。
     *
     * @param request 更新联盟球队玩法赔率参数
     */
    void update(
            UpdateLeagueTeamMarketOddsRequest request);

    /**
     * 删除指定联盟球队玩法赔率配置。
     *
     * @param request 删除联盟球队玩法赔率参数
     */
    void delete(
            DeleteLeagueTeamMarketOddsRequest request);

    /**
     * 查询指定联赛下出现过的球队。
     *
     * @param request 联赛球队查询参数
     * @return 联赛球队列表
     */
    List<ChampionLeagueTeamResponse> leagueTeams(
            ChampionLeagueTeamRequest request);
}
