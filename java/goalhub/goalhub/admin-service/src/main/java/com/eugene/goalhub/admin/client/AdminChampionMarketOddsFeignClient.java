package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

import java.util.List;

/**
 * match-service 内部管理端冠军赔率 Feign 客户端。
 *
 * <p>封装 admin-service 到 match-service 的冠军赔率内部管理接口调用。</p>
 */
@FeignClient(
        name = "match-service",
        contextId = "adminChampionMarketOddsFeignClient"
)
public interface AdminChampionMarketOddsFeignClient {

    /**
     * 调用 match-service 分页查询冠军赔率。
     *
     * @param request 冠军赔率分页查询参数
     * @return 冠军赔率分页结果
     */
    @PostMapping("/internal/admin/championmarketodds/page")
    Result<PageResponse<ChampionMarketOddsResponse>> page(
            @RequestBody ChampionMarketOddsPageRequest request);

    /**
     * 调用 match-service 查询指定联赛下出现过的球队。
     *
     * @param request 联赛球队查询参数
     * @return 联赛球队列表
     */
    @PostMapping("/internal/admin/championmarketodds/leagueteams")
    Result<List<ChampionLeagueTeamResponse>> leagueTeams(
            @RequestBody ChampionLeagueTeamRequest request);

    /**
     * 调用 match-service 新增冠军赔率。
     *
     * @param request 新增冠军赔率参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/championmarketodds/add")
    Result<Void> add(
            @RequestBody AddChampionMarketOddsRequest request);

    /**
     * 调用 match-service 更新冠军赔率。
     *
     * @param request 更新冠军赔率参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/championmarketodds/update")
    Result<Void> update(
            @RequestBody UpdateChampionMarketOddsRequest request);

    /**
     * 调用 match-service 删除冠军赔率。
     *
     * @param request 删除冠军赔率参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/championmarketodds/delete")
    Result<Void> delete(
            @RequestBody DeleteChampionMarketOddsRequest request);
}
