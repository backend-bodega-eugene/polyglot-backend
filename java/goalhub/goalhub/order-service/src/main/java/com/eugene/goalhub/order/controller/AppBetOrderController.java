package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AppBetOrderService;
import dto.PlaceBetOrderRequest;
import dto.PlaceBetOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端投注订单接口。
 *
 * <p>提供前端用户提交投注订单的 HTTP 接口。</p>
 */
@Tag(name = "前端投注订单接口", description = "前端App投注下单接口")
@RestController
@RequestMapping("/order/bet/orders")
public class AppBetOrderController {

    /**
     * 前端投注订单服务。
     */
    private final AppBetOrderService appBetOrderService;

    /**
     * 创建前端投注订单接口。
     *
     * @param appBetOrderService 前端投注订单服务
     */
    public AppBetOrderController(
            AppBetOrderService appBetOrderService) {
        this.appBetOrderService = appBetOrderService;
    }

    /**
     * 提交投注订单。
     *
     * @param userId  当前登录用户 ID
     * @param request 投注下单参数
     * @return 投注下单结果
     */
    @Operation(summary = "提交投注订单", description = "当前登录用户根据赛事玩法选项和投注金额提交投注订单。")
    @PostMapping("/place")
    public Result<PlaceBetOrderResponse> placeOrder(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "下注请求", required = true)
            @Valid @RequestBody PlaceBetOrderRequest request) {

        return Result.success(
                appBetOrderService.placeOrder(userId, request)
        );
    }
}
