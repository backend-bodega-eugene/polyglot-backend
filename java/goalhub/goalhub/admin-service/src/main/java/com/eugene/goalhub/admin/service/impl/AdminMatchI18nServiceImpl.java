package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminMatchI18nFeignClient;
import com.eugene.goalhub.admin.service.AdminMatchI18nService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台赛事国际化管理服务实现。
 * <p>
 * 当前服务通过 Feign 调用 match-service 的内部赛事国际化接口。
 */
@Service
public class AdminMatchI18nServiceImpl implements AdminMatchI18nService {

    /**
     * 后台赛事国际化远程调用客户端。
     */
    private final AdminMatchI18nFeignClient adminMatchI18nFeignClient;

    /**
     * 创建后台赛事国际化管理服务实现。
     *
     * @param adminMatchI18nFeignClient 后台赛事国际化远程调用客户端
     */
    public AdminMatchI18nServiceImpl(AdminMatchI18nFeignClient adminMatchI18nFeignClient) {
        this.adminMatchI18nFeignClient = adminMatchI18nFeignClient;
    }

    /**
     * 查询联赛国际化配置列表。
     *
     * @param request 联赛国际化查询条件
     * @return 联赛国际化配置列表
     */
    @Override
    public List<LeagueI18nResponse> listLeagueI18n(LeagueI18nListRequest request) {
        return FeignResultSupport.data(adminMatchI18nFeignClient.listLeagueI18n(request));
    }

    /**
     * 新增联赛国际化配置。
     *
     * @param request 联赛国际化新增参数
     */
    @Override
    public void addLeagueI18n(AddLeagueI18nRequest request) {
        FeignResultSupport.checkSuccess(adminMatchI18nFeignClient.addLeagueI18n(request));
    }

    /**
     * 更新联赛国际化配置。
     *
     * @param request 联赛国际化更新参数
     */
    @Override
    public void updateLeagueI18n(UpdateLeagueI18nRequest request) {
        FeignResultSupport.checkSuccess(adminMatchI18nFeignClient.updateLeagueI18n(request));
    }

    /**
     * 删除联赛国际化配置。
     *
     * @param request 联赛国际化删除参数
     */
    @Override
    public void deleteLeagueI18n(DeleteLeagueI18nRequest request) {
        FeignResultSupport.checkSuccess(adminMatchI18nFeignClient.deleteLeagueI18n(request));
    }

    /**
     * 查询比赛国际化配置列表。
     *
     * @param request 比赛国际化查询条件
     * @return 比赛国际化配置列表
     */
    @Override
    public List<MatchI18nResponse> listMatchI18n(MatchI18nListRequest request) {
        return FeignResultSupport.data(adminMatchI18nFeignClient.listMatchI18n(request));
    }

    /**
     * 新增比赛国际化配置。
     *
     * @param request 比赛国际化新增参数
     */
    @Override
    public void addMatchI18n(AddMatchI18nRequest request) {
        FeignResultSupport.checkSuccess(adminMatchI18nFeignClient.addMatchI18n(request));
    }

    /**
     * 更新比赛国际化配置。
     *
     * @param request 比赛国际化更新参数
     */
    @Override
    public void updateMatchI18n(UpdateMatchI18nRequest request) {
        FeignResultSupport.checkSuccess(adminMatchI18nFeignClient.updateMatchI18n(request));
    }

    /**
     * 删除比赛国际化配置。
     *
     * @param request 比赛国际化删除参数
     */
    @Override
    public void deleteMatchI18n(DeleteMatchI18nRequest request) {
        FeignResultSupport.checkSuccess(adminMatchI18nFeignClient.deleteMatchI18n(request));
    }

    /**
     * 查询球队国际化配置列表。
     *
     * @param request 球队国际化查询条件
     * @return 球队国际化配置列表
     */
    @Override
    public List<TeamI18nResponse> listTeamI18n(TeamI18nListRequest request) {
        return FeignResultSupport.data(adminMatchI18nFeignClient.listTeamI18n(request));
    }

    /**
     * 新增球队国际化配置。
     *
     * @param request 球队国际化新增参数
     */
    @Override
    public void addTeamI18n(AddTeamI18nRequest request) {
        FeignResultSupport.checkSuccess(adminMatchI18nFeignClient.addTeamI18n(request));
    }

    /**
     * 更新球队国际化配置。
     *
     * @param request 球队国际化更新参数
     */
    @Override
    public void updateTeamI18n(UpdateTeamI18nRequest request) {
        FeignResultSupport.checkSuccess(adminMatchI18nFeignClient.updateTeamI18n(request));
    }

    /**
     * 删除球队国际化配置。
     *
     * @param request 球队国际化删除参数
     */
    @Override
    public void deleteTeamI18n(DeleteTeamI18nRequest request) {
        FeignResultSupport.checkSuccess(adminMatchI18nFeignClient.deleteTeamI18n(request));
    }
}
