package com.eugene.goalhub.order.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * match-service 订单侧赛事快照 Feign 客户端。
 *
 * <p>用于订单服务在下单时查询赛事、玩法、选项和赔率的只读快照。</p>
 */
@FeignClient(
        name = "match-service",
        contextId = "OrderMatchFeignClient"
)
public interface OrderMatchFeignClient {

    /**
     * 查询下单用赛事玩法选项快照。
     *
     * @param matchMarketOptionId 赛事玩法选项 ID
     * @return 赛事玩法选项快照结果
     */
    @GetMapping("/internal/order/matches/options/{matchMarketOptionId}/snapshot")
    Result<OrderMatchOptionSnapshotResponse> getMatchOptionSnapshot(
            @PathVariable("matchMarketOptionId") Long matchMarketOptionId
    );

    /**
     * 查询下单用冠军赔率快照。
     *
     * @param request 冠军赔率快照查询参数
     * @return 冠军赔率快照结果
     */
    @PostMapping("/internal/order/championodds/snapshot")
    Result<ChampionOddsSnapshotResponse> getChampionOddsSnapshot(
            @RequestBody ChampionOddsSnapshotRequest request);
//    @PostMapping("/internal/order/account/deductdefaultusdt")
//    Result<DeductDefaultAccountResponse> deductDefaultUsdt(
//            @RequestBody DeductDefaultAccountRequest request);
}
