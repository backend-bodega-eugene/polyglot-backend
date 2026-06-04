package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AppBetOrderService;
import dto.PlaceBetOrderRequest;
import dto.PlaceBetOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端投注订单接口。
 */
@Tag(name = "前端投注订单接口", description = "前端App投注下单接口")
@RestController
@RequestMapping("/order/bet/orders")
public class AppBetOrderController {

    private final AppBetOrderService appBetOrderService;

    public AppBetOrderController(
            AppBetOrderService appBetOrderService) {
        this.appBetOrderService = appBetOrderService;
    }

    @Operation(summary = "提交投注订单")
    @PostMapping("/place")
    public Result<PlaceBetOrderResponse> placeOrder(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "下注请求", required = true)
            @RequestBody PlaceBetOrderRequest request) {

        return Result.success(
                appBetOrderService.placeOrder(userId, request)
        );
    }
}