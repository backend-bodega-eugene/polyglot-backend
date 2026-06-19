package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminLeagueTeamMarketOddsFeignClient;
import com.eugene.goalhub.admin.service.AdminLeagueTeamMarketOddsService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台联盟球队玩法赔率管理服务实现。
 *
 * <p>当前服务通过 Feign 调用 match-service 的内部联盟球队玩法赔率接口。</p>
 */
@Service
public class AdminLeagueTeamMarketOddsServiceImpl
        implements AdminLeagueTeamMarketOddsService {

    /**
     * 联盟球队玩法赔率远程调用客户端。
     */
    private final AdminLeagueTeamMarketOddsFeignClient
            adminLeagueTeamMarketOddsFeignClient;

    /**
     * 创建后台联盟球队玩法赔率管理服务实现。
     *
     * @param adminLeagueTeamMarketOddsFeignClient 联盟球队玩法赔率远程调用客户端
     */
    public AdminLeagueTeamMarketOddsServiceImpl(
            AdminLeagueTeamMarketOddsFeignClient adminLeagueTeamMarketOddsFeignClient) {

        this.adminLeagueTeamMarketOddsFeignClient =
                adminLeagueTeamMarketOddsFeignClient;
    }

    /**
     * 分页查询联盟球队玩法赔率配置列表。
     *
     * @param request 联盟球队玩法赔率分页查询参数
     * @return 联盟球队玩法赔率分页结果
     */
    @Override
    public PageResponse<LeagueTeamMarketOddsResponse> page(
            LeagueTeamMarketOddsPageRequest request) {

        return FeignResultSupport.data(
                adminLeagueTeamMarketOddsFeignClient.page(request)
        );
    }

    /**
     * 新增联盟球队玩法赔率配置。
     *
     * @param request 新增联盟球队玩法赔率参数
     */
    @Override
    public void add(
            AddLeagueTeamMarketOddsRequest request) {

        FeignResultSupport.checkSuccess(
                adminLeagueTeamMarketOddsFeignClient.add(request)
        );
    }

    /**
     * 更新联盟球队玩法赔率配置。
     *
     * @param request 更新联盟球队玩法赔率参数
     */
    @Override
    public void update(
            UpdateLeagueTeamMarketOddsRequest request) {

        FeignResultSupport.checkSuccess(
                adminLeagueTeamMarketOddsFeignClient.update(request)
        );
    }

    /**
     * 删除指定联盟球队玩法赔率配置。
     *
     * @param request 删除联盟球队玩法赔率参数
     */
    @Override
    public void delete(
            DeleteLeagueTeamMarketOddsRequest request) {

        FeignResultSupport.checkSuccess(
                adminLeagueTeamMarketOddsFeignClient.delete(request)
        );
    }

    /**
     * 查询指定联赛下出现过的球队。
     *
     * @param request 联赛球队查询参数
     * @return 联赛球队列表
     */
    @Override
    public List<ChampionLeagueTeamResponse> leagueTeams(
            ChampionLeagueTeamRequest request) {

        return FeignResultSupport.data(
                adminLeagueTeamMarketOddsFeignClient
                        .leagueTeams(request)
        );
    }
}
