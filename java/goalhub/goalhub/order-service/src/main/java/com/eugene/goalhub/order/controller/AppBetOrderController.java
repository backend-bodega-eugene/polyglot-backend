package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AppBetOrderService;
import dto.*;
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

    /**
     * 分页查询当前用户未结算投注订单。
     *
     * @param userId  当前登录用户 ID
     * @param request 投注订单分页查询参数
     * @return 未结算投注订单分页结果
     */
    @Operation(summary = "查询未结算投注订单", description = "分页查询当前登录用户已下单但未结算的投注订单。")
    @GetMapping("/unsettled")
    public Result<PageResponse<AppBetOrderResponse>> pageUnsettledOrders(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "投注订单分页查询参数", required = true)
            @RequestBody AppBetOrderPageRequest request) {

        return Result.success(
                appBetOrderService.pageUnsettledOrders(userId, request)
        );
    }

    /**
     * 分页查询当前用户已结算投注订单。
     *
     * @param userId  当前登录用户 ID
     * @param request 投注订单分页查询参数
     * @return 已结算投注订单分页结果
     */
    @Operation(summary = "查询已结算投注订单", description = "分页查询当前登录用户已经结算的投注订单。")
    @GetMapping("/settled")
    public Result<PageResponse<AppBetOrderResponse>> pageSettledOrders(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "投注订单分页查询参数", required = true)
            @RequestBody AppBetOrderPageRequest request) {

        return Result.success(
                appBetOrderService.pageSettledOrders(userId, request)
        );
    }

    /**
     * 分页查询当前用户全部投注订单。
     *
     * @param userId  当前登录用户 ID
     * @param request 投注订单分页查询参数
     * @return 投注订单分页结果
     */
    @Operation(summary = "查询我的投注订单", description = "分页查询当前登录用户全部投注订单，支持订单号、玩法、选项关键字和下单时间筛选。")
    @PostMapping("/page")
    public Result<PageResponse<AppBetOrderResponse>> pageMyOrders(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "投注订单分页查询参数", required = true)
            @RequestBody AppBetOrderPageRequest request) {

        return Result.success(
                appBetOrderService.pageMyOrders(userId, request)
        );
    }

    /**
     * 提交冠军投注订单。
     *
     * @param userId  当前登录用户 ID
     * @param request 冠军下注参数
     * @return 冠军投注下单结果
     */
    @Operation(summary = "提交冠军投注订单", description = "当前登录用户根据冠军赔率和投注金额提交冠军投注订单。")
    @PostMapping("/placechampion")
    public Result<PlaceBetOrderResponse> placeChampionOrder(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "冠军下注请求", required = true)
            @Valid @RequestBody PlaceChampionBetOrderRequest request) {

        return Result.success(
                appBetOrderService.placeChampionOrder(userId, request)
        );
    }
}
