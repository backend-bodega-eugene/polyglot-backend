package com.eugene.goalhub.order.client;

import dto.DeductDefaultAccountRequest;
import dto.DeductDefaultAccountResponse;
import dto.OrderMatchOptionSnapshotResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import response.Result;

@FeignClient(
        name = "match-service",
        contextId = "OrderMatchFeignClient"
)
public interface OrderMatchFeignClient {

    @GetMapping("/internal/order/matches/options/{matchMarketOptionId}/snapshot")
    Result<OrderMatchOptionSnapshotResponse> getMatchOptionSnapshot(
            @PathVariable("matchMarketOptionId") Long matchMarketOptionId
    );
//    @PostMapping("/internal/order/account/deductdefaultusdt")
//    Result<DeductDefaultAccountResponse> deductDefaultUsdt(
//            @RequestBody DeductDefaultAccountRequest request);
}