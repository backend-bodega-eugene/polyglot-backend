package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AppDepositOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端充值订单接口。
 *
 * <p>提供当前登录用户提交充值申请和分页查询充值申请的能力。</p>
 */
@Tag(name = "前端充值订单接口", description = "前端App充值申请接口")
@RestController
@RequestMapping("/order/depositorder")
public class AppDepositOrderController {

    /**
     * 前端充值订单服务。
     */
    private final AppDepositOrderService appDepositOrderService;

    /**
     * 创建前端充值订单接口实例。
     *
     * @param appDepositOrderService 前端充值订单服务
     */
    public AppDepositOrderController(
            AppDepositOrderService appDepositOrderService) {
        this.appDepositOrderService = appDepositOrderService;
    }

    /**
     * 提交充值申请。
     *
     * @param userId  当前登录用户 ID
     * @param request 充值申请参数
     * @return 充值订单
     */
    @Operation(summary = "提交充值申请", description = "当前登录用户提交充值申请订单。")
    @PostMapping("/create")
    public Result<AppDepositOrderResponse> create(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "充值申请参数", required = true)
            @Valid @RequestBody AppDepositOrderCreateRequest request) {

        return Result.success(
                appDepositOrderService.create(userId, request)
        );
    }

    /**
     * 分页查询当前用户充值申请。
     *
     * @param userId  当前登录用户 ID
     * @param request 充值订单分页查询参数
     * @return 充值订单分页数据
     */
    @Operation(summary = "查询我的充值申请", description = "分页查询当前登录用户的充值申请订单。")
    @PostMapping("/page")
    public Result<PageResponse<AppDepositOrderResponse>> page(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "充值订单分页查询参数", required = true)
            @RequestBody AppDepositOrderPageRequest request) {

        return Result.success(
                appDepositOrderService.page(userId, request)
        );
    }
}
