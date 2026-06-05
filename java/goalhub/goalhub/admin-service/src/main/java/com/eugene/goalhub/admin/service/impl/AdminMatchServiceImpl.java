package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminMatchFeignClient;
import com.eugene.goalhub.admin.service.AdminMatchService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台赛事基础数据管理服务实现。
 * <p>
 * 当前服务通过 Feign 调用 match-service 的内部联赛、比赛和球队接口。
 * 本类只负责后台服务编排和远程调用结果校验，不直接操作赛事数据表。
 */
@Service
public class AdminMatchServiceImpl implements AdminMatchService {

    /**
     * 后台赛事基础数据远程调用客户端。
     */
    private final AdminMatchFeignClient adminMatchFeignClient;

    /**
     * 创建后台赛事基础数据管理服务实现。
     *
     * @param adminMatchFeignClient 后台赛事基础数据远程调用客户端
     */
    public AdminMatchServiceImpl(
            AdminMatchFeignClient adminMatchFeignClient) {
        this.adminMatchFeignClient = adminMatchFeignClient;
    }

    /**
     * 分页查询联赛。
     *
     * @param request 联赛分页查询条件
     * @return 联赛分页数据
     */
    @Override
    public PageResponse<AdminLeagueResponse> leaguePage(
            LeaguePageRequest request) {

        return FeignResultSupport.data(adminMatchFeignClient.leaguePage(request));
    }

    /**
     * 新增联赛。
     *
     * @param request 联赛新增参数
     */
    @Override
    public void addLeague(
            AddLeagueRequest request) {

        FeignResultSupport.checkSuccess(adminMatchFeignClient.addLeague(request));
    }

    /**
     * 更新联赛。
     *
     * @param request 联赛更新参数
     */
    @Override
    public void updateLeague(
            UpdateLeagueRequest request) {

        FeignResultSupport.checkSuccess(adminMatchFeignClient.updateLeague(request));
    }

    /**
     * 删除联赛。
     *
     * @param request 联赛删除参数
     */
    @Override
    public void deleteLeague(
            DeleteLeagueRequest request) {

        FeignResultSupport.checkSuccess(adminMatchFeignClient.deleteLeague(request));
    }

    /**
     * 分页查询比赛。
     *
     * @param request 比赛分页查询条件
     * @return 比赛分页数据
     */
    @Override
    public PageResponse<AdminMatchResponse> matchPage(
            MatchPageRequest request) {

        return FeignResultSupport.data(adminMatchFeignClient.matchPage(request));
    }

    /**
     * 新增比赛。
     *
     * @param request 比赛新增参数
     */
    @Override
    public void addMatch(
            AddMatchRequest request) {

        FeignResultSupport.checkSuccess(adminMatchFeignClient.addMatch(request));
    }

    /**
     * 更新比赛。
     *
     * @param request 比赛更新参数
     */
    @Override
    public void updateMatch(
            UpdateMatchRequest request) {

        FeignResultSupport.checkSuccess(adminMatchFeignClient.updateMatch(request));
    }

    /**
     * 删除比赛。
     *
     * @param request 比赛删除参数
     */
    @Override
    public void deleteMatch(
            DeleteMatchRequest request) {

        FeignResultSupport.checkSuccess(adminMatchFeignClient.deleteMatch(request));
    }

    /**
     * 分页查询球队。
     *
     * @param request 球队分页查询条件
     * @return 球队分页数据
     */
    @Override
    public PageResponse<AdminTeamResponse> teamPage(TeamPageRequest request) {
        return FeignResultSupport.data(adminMatchFeignClient.teamPage(request));
    }

    /**
     * 新增球队。
     *
     * @param request 球队新增参数
     */
    @Override
    public void addTeam(AddTeamRequest request) {
        FeignResultSupport.checkSuccess(adminMatchFeignClient.addTeam(request));
    }

    /**
     * 更新球队。
     *
     * @param request 球队更新参数
     */
    @Override
    public void updateTeam(UpdateTeamRequest request) {
        FeignResultSupport.checkSuccess(adminMatchFeignClient.updateTeam(request));
    }

    /**
     * 删除球队。
     *
     * @param request 球队删除参数
     */
    @Override
    public void deleteTeam(DeleteTeamRequest request) {
        FeignResultSupport.checkSuccess(adminMatchFeignClient.deleteTeam(request));
    }
}
