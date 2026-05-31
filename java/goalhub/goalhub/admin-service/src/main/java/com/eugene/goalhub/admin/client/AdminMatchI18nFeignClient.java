package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

import java.util.List;

/**
 * match-service 内部管理端赛事国际化 Feign 客户端。
 */
@FeignClient(
        name = "match-service",
        contextId = "adminMatchI18nFeignClient"
)
public interface AdminMatchI18nFeignClient {

    /**
     * 查询联赛国际化配置列表。
     *
     * @param request 联赛国际化查询条件
     * @return 联赛国际化配置列表
     */
    @PostMapping("/internal/admin/matchi18n/league/list")
    Result<List<LeagueI18nResponse>> listLeagueI18n(@RequestBody LeagueI18nListRequest request);

    /**
     * 新增联赛国际化配置。
     *
     * @param request 联赛国际化新增参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchi18n/league/add")
    Result<Void> addLeagueI18n(@RequestBody AddLeagueI18nRequest request);

    /**
     * 更新联赛国际化配置。
     *
     * @param request 联赛国际化更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchi18n/league/update")
    Result<Void> updateLeagueI18n(@RequestBody UpdateLeagueI18nRequest request);

    /**
     * 删除联赛国际化配置。
     *
     * @param request 联赛国际化删除参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchi18n/league/delete")
    Result<Void> deleteLeagueI18n(@RequestBody DeleteLeagueI18nRequest request);

    /**
     * 查询比赛国际化配置列表。
     *
     * @param request 比赛国际化查询条件
     * @return 比赛国际化配置列表
     */
    @PostMapping("/internal/admin/matchi18n/match/list")
    Result<List<MatchI18nResponse>> listMatchI18n(@RequestBody MatchI18nListRequest request);

    /**
     * 新增比赛国际化配置。
     *
     * @param request 比赛国际化新增参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchi18n/match/add")
    Result<Void> addMatchI18n(@RequestBody AddMatchI18nRequest request);

    /**
     * 更新比赛国际化配置。
     *
     * @param request 比赛国际化更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchi18n/match/update")
    Result<Void> updateMatchI18n(@RequestBody UpdateMatchI18nRequest request);

    /**
     * 删除比赛国际化配置。
     *
     * @param request 比赛国际化删除参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchi18n/match/delete")
    Result<Void> deleteMatchI18n(@RequestBody DeleteMatchI18nRequest request);

    /**
     * 查询球队国际化配置列表。
     *
     * @param request 球队国际化查询条件
     * @return 球队国际化配置列表
     */
    @PostMapping("/internal/admin/matchi18n/team/list")
    Result<List<TeamI18nResponse>> listTeamI18n(@RequestBody TeamI18nListRequest request);

    /**
     * 新增球队国际化配置。
     *
     * @param request 球队国际化新增参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchi18n/team/add")
    Result<Void> addTeamI18n(@RequestBody AddTeamI18nRequest request);

    /**
     * 更新球队国际化配置。
     *
     * @param request 球队国际化更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchi18n/team/update")
    Result<Void> updateTeamI18n(@RequestBody UpdateTeamI18nRequest request);

    /**
     * 删除球队国际化配置。
     *
     * @param request 球队国际化删除参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchi18n/team/delete")
    Result<Void> deleteTeamI18n(@RequestBody DeleteTeamI18nRequest request);
}
