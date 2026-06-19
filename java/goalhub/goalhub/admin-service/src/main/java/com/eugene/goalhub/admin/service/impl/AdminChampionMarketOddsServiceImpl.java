package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminChampionMarketOddsFeignClient;
import com.eugene.goalhub.admin.service.AdminChampionMarketOddsService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台冠军赔率管理服务实现。
 *
 * <p>当前服务通过 Feign 调用 match-service 的内部冠军赔率接口。</p>
 */
@Service
public class AdminChampionMarketOddsServiceImpl implements AdminChampionMarketOddsService {

    /**
     * 冠军赔率远程调用客户端。
     */
    private final AdminChampionMarketOddsFeignClient adminChampionMarketOddsFeignClient;

    /**
     * 创建后台冠军赔率管理服务实现。
     *
     * @param adminChampionMarketOddsFeignClient 冠军赔率远程调用客户端
     */
    public AdminChampionMarketOddsServiceImpl(
            AdminChampionMarketOddsFeignClient adminChampionMarketOddsFeignClient) {
        this.adminChampionMarketOddsFeignClient = adminChampionMarketOddsFeignClient;
    }

    /**
     * 分页查询冠军赔率配置列表。
     *
     * @param request 冠军赔率分页查询参数
     * @return 冠军赔率分页结果
     */
    @Override
    public PageResponse<ChampionMarketOddsResponse> page(
            ChampionMarketOddsPageRequest request) {

        return FeignResultSupport.data(
                adminChampionMarketOddsFeignClient.page(request));
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
                adminChampionMarketOddsFeignClient.leagueTeams(request));
    }

    /**
     * 新增冠军赔率配置。
     *
     * @param request 新增冠军赔率参数
     */
    @Override
    public void add(
            AddChampionMarketOddsRequest request) {

        FeignResultSupport.checkSuccess(
                adminChampionMarketOddsFeignClient.add(request));
    }

    /**
     * 更新冠军赔率配置。
     *
     * @param request 更新冠军赔率参数
     */
    @Override
    public void update(
            UpdateChampionMarketOddsRequest request) {

        FeignResultSupport.checkSuccess(
                adminChampionMarketOddsFeignClient.update(request));
    }

    /**
     * 删除指定冠军赔率配置。
     *
     * @param request 删除冠军赔率参数
     */
    @Override
    public void delete(
            DeleteChampionMarketOddsRequest request) {

        FeignResultSupport.checkSuccess(
                adminChampionMarketOddsFeignClient.delete(request));
    }
}
