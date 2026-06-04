package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.OrderMatchSnapshotService;
import dto.OrderMatchOptionSnapshotResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "订单内部赛事快照接口", description = "供order-service下单时查询赛事赔率快照")
@RestController
@RequestMapping("/internal/order/matches")
public class InternalOrderMatchSnapshotController {

    private final OrderMatchSnapshotService orderMatchSnapshotService;

    public InternalOrderMatchSnapshotController(
            OrderMatchSnapshotService orderMatchSnapshotService) {
        this.orderMatchSnapshotService = orderMatchSnapshotService;
    }

    @Operation(summary = "查询下单用赛事玩法赔率快照")
    @GetMapping("/options/{matchMarketOptionId}/snapshot")
    public Result<OrderMatchOptionSnapshotResponse> getOrderSnapshot(
            @PathVariable("matchMarketOptionId") Long matchMarketOptionId) {

        return Result.success(
                orderMatchSnapshotService.getOrderSnapshot(matchMarketOptionId)
        );
    }
}