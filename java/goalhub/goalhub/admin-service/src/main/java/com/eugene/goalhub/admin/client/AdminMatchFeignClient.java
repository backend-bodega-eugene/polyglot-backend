package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * match-service 内部管理端赛事基础数据 Feign 客户端。
 */
@FeignClient(
        name = "match-service",
        contextId = "adminMatchFeignClient"
)
public interface AdminMatchFeignClient {

    /**
     * 分页查询联赛。
     *
     * @param request 联赛分页查询条件
     * @return 联赛分页数据
     */
    @PostMapping("/internal/admin/league/page")
    Result<PageResponse<AdminLeagueResponse>> leaguePage(
            @RequestBody LeaguePageRequest request);

    /**
     * 新增联赛。
     *
     * @param request 联赛新增参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/league/add")
    Result<Void> addLeague(
            @RequestBody AddLeagueRequest request);

    /**
     * 更新联赛。
     *
     * @param request 联赛更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/league/update")
    Result<Void> updateLeague(
            @RequestBody UpdateLeagueRequest request);

    /**
     * 删除联赛。
     *
     * @param request 联赛删除参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/league/delete")
    Result<Void> deleteLeague(
            @RequestBody DeleteLeagueRequest request);

    /**
     * 分页查询比赛。
     *
     * @param request 比赛分页查询条件
     * @return 比赛分页数据
     */
    @PostMapping("/internal/admin/match/page")
    Result<PageResponse<AdminMatchResponse>> matchPage(
            @RequestBody MatchPageRequest request);

    /**
     * 新增比赛。
     *
     * @param request 比赛新增参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/match/add")
    Result<Void> addMatch(
            @RequestBody AddMatchRequest request);

    /**
     * 更新比赛。
     *
     * @param request 比赛更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/match/update")
    Result<Void> updateMatch(
            @RequestBody UpdateMatchRequest request);

    /**
     * 删除比赛。
     *
     * @param request 比赛删除参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/match/delete")
    Result<Void> deleteMatch(
            @RequestBody DeleteMatchRequest request);

    /**
     * 分页查询球队。
     *
     * @param request 球队分页查询条件
     * @return 球队分页数据
     */
    @PostMapping("/internal/admin/match/team/page")
    Result<PageResponse<AdminTeamResponse>> teamPage(
            @RequestBody TeamPageRequest request);

    /**
     * 新增球队。
     *
     * @param request 球队新增参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/match/team/add")
    Result<Void> addTeam(
            @RequestBody AddTeamRequest request);

    /**
     * 更新球队。
     *
     * @param request 球队更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/match/team/update")
    Result<Void> updateTeam(
            @RequestBody UpdateTeamRequest request);

    /**
     * 删除球队。
     *
     * @param request 球队删除参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/match/team/delete")
    Result<Void> deleteTeam(
            @RequestBody DeleteTeamRequest request);
}
