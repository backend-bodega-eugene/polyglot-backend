package com.eugene.goalhub.match.service;

import dto.*;

/**
 * 后台赛事基础数据管理服务。
 *
 * <p>负责后台联赛、比赛和球队基础数据的查询与维护。</p>
 */
public interface AdminMatchService {

    /**
     * 分页查询联赛。
     *
     * @param request 联赛分页查询条件
     * @return 联赛分页数据
     */
    PageResponse<AdminLeagueResponse> leaguePage(LeaguePageRequest request);

    /**
     * 新增联赛。
     *
     * @param request 联赛新增参数
     */
    void addLeague(AddLeagueRequest request);

    /**
     * 更新联赛。
     *
     * @param request 联赛更新参数
     */
    void updateLeague(UpdateLeagueRequest request);

    /**
     * 删除联赛。
     *
     * @param request 联赛删除参数
     */
    void deleteLeague(DeleteLeagueRequest request);

    /**
     * 分页查询比赛。
     *
     * @param request 比赛分页查询条件
     * @return 比赛分页数据
     */
    PageResponse<AdminMatchResponse> matchPage(MatchPageRequest request);

    /**
     * 新增比赛。
     *
     * @param request 比赛新增参数
     */
    void addMatch(AddMatchRequest request);

    /**
     * 更新比赛。
     *
     * @param request 比赛更新参数
     */
    void updateMatch(UpdateMatchRequest request);

    /**
     * 删除比赛。
     *
     * @param request 比赛删除参数
     */
    void deleteMatch(DeleteMatchRequest request);

    /**
     * 分页查询球队。
     *
     * @param request 球队分页查询条件
     * @return 球队分页数据
     */
    PageResponse<AdminTeamResponse> teamPage(TeamPageRequest request);

    /**
     * 新增球队。
     *
     * @param request 球队新增参数
     */
    void addTeam(AddTeamRequest request);

    /**
     * 更新球队。
     *
     * @param request 球队更新参数
     */
    void updateTeam(UpdateTeamRequest request);

    /**
     * 删除球队。
     *
     * @param request 球队删除参数
     */
    void deleteTeam(DeleteTeamRequest request);
}
