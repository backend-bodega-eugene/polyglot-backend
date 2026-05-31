package com.eugene.goalhub.admin.service;

import dto.*;

import java.util.List;

/**
 * 后台赛事国际化管理服务。
 */
public interface AdminMatchI18nService {

    /**
     * 查询联赛国际化配置列表。
     *
     * @param request 联赛国际化查询条件
     * @return 联赛国际化配置列表
     */
    List<LeagueI18nResponse> listLeagueI18n(LeagueI18nListRequest request);

    /**
     * 新增联赛国际化配置。
     *
     * @param request 联赛国际化新增参数
     */
    void addLeagueI18n(AddLeagueI18nRequest request);

    /**
     * 更新联赛国际化配置。
     *
     * @param request 联赛国际化更新参数
     */
    void updateLeagueI18n(UpdateLeagueI18nRequest request);

    /**
     * 删除联赛国际化配置。
     *
     * @param request 联赛国际化删除参数
     */
    void deleteLeagueI18n(DeleteLeagueI18nRequest request);

    /**
     * 查询比赛国际化配置列表。
     *
     * @param request 比赛国际化查询条件
     * @return 比赛国际化配置列表
     */
    List<MatchI18nResponse> listMatchI18n(MatchI18nListRequest request);

    /**
     * 新增比赛国际化配置。
     *
     * @param request 比赛国际化新增参数
     */
    void addMatchI18n(AddMatchI18nRequest request);

    /**
     * 更新比赛国际化配置。
     *
     * @param request 比赛国际化更新参数
     */
    void updateMatchI18n(UpdateMatchI18nRequest request);

    /**
     * 删除比赛国际化配置。
     *
     * @param request 比赛国际化删除参数
     */
    void deleteMatchI18n(DeleteMatchI18nRequest request);

    /**
     * 查询球队国际化配置列表。
     *
     * @param request 球队国际化查询条件
     * @return 球队国际化配置列表
     */
    List<TeamI18nResponse> listTeamI18n(TeamI18nListRequest request);

    /**
     * 新增球队国际化配置。
     *
     * @param request 球队国际化新增参数
     */
    void addTeamI18n(AddTeamI18nRequest request);

    /**
     * 更新球队国际化配置。
     *
     * @param request 球队国际化更新参数
     */
    void updateTeamI18n(UpdateTeamI18nRequest request);

    /**
     * 删除球队国际化配置。
     *
     * @param request 球队国际化删除参数
     */
    void deleteTeamI18n(DeleteTeamI18nRequest request);
}
