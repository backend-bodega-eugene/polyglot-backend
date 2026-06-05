package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.OrderMatchSnapshotService;
import dto.OrderMatchOptionSnapshotResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 订单内部赛事快照接口。
 *
 * <p>供订单服务在下单前查询赛事玩法赔率快照。</p>
 */
@Tag(name = "订单内部赛事快照接口", description = "供order-service下单时查询赛事赔率快照")
@RestController
@RequestMapping("/internal/order/matches")
public class InternalOrderMatchSnapshotController {

    /**
     * 订单赛事快照服务。
     */
    private final OrderMatchSnapshotService orderMatchSnapshotService;

    /**
     * 创建订单内部赛事快照接口实例。
     *
     * @param orderMatchSnapshotService 订单赛事快照服务
     */
    public InternalOrderMatchSnapshotController(
            OrderMatchSnapshotService orderMatchSnapshotService) {
        this.orderMatchSnapshotService = orderMatchSnapshotService;
    }

    /**
     * 查询下单用赛事玩法赔率快照。
     *
     * @param matchMarketOptionId 赛事玩法赔率 ID
     * @return 下单用赛事玩法赔率快照
     */
    @Operation(summary = "查询下单用赛事玩法赔率快照", description = "根据赛事玩法赔率 ID 查询订单下单时使用的赛事、玩法、选项和赔率快照。")
    @GetMapping("/options/{matchMarketOptionId}/snapshot")
    public Result<OrderMatchOptionSnapshotResponse> getOrderSnapshot(
            @Parameter(description = "赛事玩法赔率 ID", required = true)
            @PathVariable("matchMarketOptionId") Long matchMarketOptionId) {

        return Result.success(
                orderMatchSnapshotService.getOrderSnapshot(matchMarketOptionId)
        );
    }
}
