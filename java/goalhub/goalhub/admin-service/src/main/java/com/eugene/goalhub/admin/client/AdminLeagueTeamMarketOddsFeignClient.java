package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

import java.util.List;

/**
 * match-service 内部管理端联盟球队玩法赔率 Feign 客户端。
 *
 * <p>封装 admin-service 到 match-service 的联盟球队玩法赔率内部管理接口调用。</p>
 */
@FeignClient(
        name = "match-service",
        contextId = "adminLeagueTeamMarketOddsFeignClient"
)
public interface AdminLeagueTeamMarketOddsFeignClient {

    /**
     * 调用 match-service 分页查询联盟球队玩法赔率。
     *
     * @param request 联盟球队玩法赔率分页查询参数
     * @return 联盟球队玩法赔率分页结果
     */
    @PostMapping("/internal/admin/leagueteammarketodds/page")
    Result<PageResponse<LeagueTeamMarketOddsResponse>> page(
            @RequestBody LeagueTeamMarketOddsPageRequest request);

    /**
     * 调用 match-service 新增联盟球队玩法赔率。
     *
     * @param request 新增联盟球队玩法赔率参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/leagueteammarketodds/add")
    Result<Void> add(
            @RequestBody AddLeagueTeamMarketOddsRequest request);

    /**
     * 调用 match-service 更新联盟球队玩法赔率。
     *
     * @param request 更新联盟球队玩法赔率参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/leagueteammarketodds/update")
    Result<Void> update(
            @RequestBody UpdateLeagueTeamMarketOddsRequest request);

    /**
     * 调用 match-service 删除联盟球队玩法赔率。
     *
     * @param request 删除联盟球队玩法赔率参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/leagueteammarketodds/delete")
    Result<Void> delete(
            @RequestBody DeleteLeagueTeamMarketOddsRequest request);

    /**
     * 调用 match-service 查询指定联赛下出现过的球队。
     *
     * @param request 联赛球队查询参数
     * @return 联赛球队列表
     */
    @PostMapping("/internal/admin/leagueteammarketodds/leagueteams")
    Result<List<ChampionLeagueTeamResponse>> leagueTeams(
            @RequestBody ChampionLeagueTeamRequest request);
}
