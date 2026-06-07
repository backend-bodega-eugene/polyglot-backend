package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AppWithdrawOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端提现订单接口。
 *
 * <p>提供当前登录用户提交提现申请和分页查询提现申请的能力。</p>
 */
@Tag(name = "前端提现订单接口", description = "前端App提现申请接口")
@RestController
@RequestMapping("/order/withdraworder")
public class AppWithdrawOrderController {

    /**
     * 前端提现订单服务。
     */
    private final AppWithdrawOrderService appWithdrawOrderService;

    /**
     * 创建前端提现订单接口实例。
     *
     * @param appWithdrawOrderService 前端提现订单服务
     */
    public AppWithdrawOrderController(
            AppWithdrawOrderService appWithdrawOrderService) {
        this.appWithdrawOrderService = appWithdrawOrderService;
    }

    /**
     * 提交提现申请。
     *
     * @param userId  当前登录用户 ID
     * @param request 提现申请参数
     * @return 提现订单
     */
    @Operation(summary = "提交提现申请", description = "当前登录用户提交提现申请订单，并冻结对应余额。")
    @PostMapping("/create")
    public Result<AppWithdrawOrderResponse> create(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "提现申请参数", required = true)
            @Valid @RequestBody AppWithdrawOrderCreateRequest request) {

        return Result.success(
                appWithdrawOrderService.create(userId, request)
        );
    }

    /**
     * 分页查询当前用户提现申请。
     *
     * @param userId  当前登录用户 ID
     * @param request 提现订单分页查询参数
     * @return 提现订单分页数据
     */
    @Operation(summary = "查询我的提现申请", description = "分页查询当前登录用户的提现申请订单。")
    @PostMapping("/page")
    public Result<PageResponse<AppWithdrawOrderResponse>> page(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "提现订单分页查询参数", required = true)
            @RequestBody AppWithdrawOrderPageRequest request) {

        return Result.success(
                appWithdrawOrderService.page(userId, request)
        );
    }
}
