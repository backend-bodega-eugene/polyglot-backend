package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AdminBetOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 内部后台投注订单管理接口。
 *
 * <p>提供 admin-service 远程调用的订单查询、明细查询、审核、冻结和结算接口。</p>
 */
@Tag(name = "内部后台投注订单管理", description = "提供后台投注订单查询、明细查询、审核、冻结和结算接口")
@RestController
@RequestMapping("/internal/admin/order")
public class InternalAdminBetOrderController {

    /**
     * 后台投注订单服务。
     */
    private final AdminBetOrderService adminBetOrderService;

    /**
     * 创建内部后台投注订单管理接口。
     *
     * @param adminBetOrderService 后台投注订单服务
     */
    public InternalAdminBetOrderController(
            AdminBetOrderService adminBetOrderService) {

        this.adminBetOrderService = adminBetOrderService;
    }

    /**
     * 分页查询投注订单。
     *
     * @param request 投注订单分页查询条件
     * @return 投注订单分页结果
     */
    @Operation(summary = "分页查询投注订单", description = "按后台筛选条件分页查询投注订单。")
    @PostMapping("/page")
    public Result<PageResponse<AdminBetOrderResponse>> orderPage(
            @Parameter(description = "投注订单分页查询条件", required = true)
            @Valid @RequestBody AdminBetOrderPageRequest request) {

        return Result.success(
                adminBetOrderService.orderPage(request)
        );
    }

    /**
     * 分页查询投注订单明细。
     *
     * @param request 投注订单明细分页查询条件
     * @return 投注订单明细分页结果
     */
    @Operation(summary = "分页查询投注订单明细", description = "按订单条件分页查询投注订单明细。")
    @PostMapping("/item/page")
    public Result<PageResponse<AdminBetOrderItemResponse>> orderItemPage(
            @Parameter(description = "投注订单明细分页查询条件", required = true)
            @Valid @RequestBody AdminBetOrderItemPageRequest request) {

        return Result.success(
                adminBetOrderService.orderItemPage(request)
        );
    }

    /**
     * 审核投注订单。
     *
     * @param request 投注订单审核参数
     * @return 空结果
     */
    @Operation(summary = "审核投注订单", description = "审核待判定投注订单并记录审核信息。")
    @PostMapping("/review")
    public Result<Void> reviewOrder(
//            @RequestHeader("X-Admin-Id") Long adminId,
//            @RequestHeader("X-Admin-Username") String adminUsername,
            @Parameter(description = "投注订单审核参数", required = true)
            @Valid @RequestBody AdminBetOrderReviewRequest request) {

        adminBetOrderService.reviewOrder(
                request,
                request.getAdminId(),
                request.getAdminUsername()
        );

        return Result.success();
    }

    /**
     * 冻结投注订单。
     *
     * @param request 投注订单冻结参数
     * @return 空结果
     */
    @Operation(summary = "冻结投注订单", description = "冻结待判定投注订单并记录审核信息。")
    @PostMapping("/freeze")
    public Result<Void> freezeOrder(
//            @RequestHeader("X-Admin-Id") Long adminId,
//            @RequestHeader("X-Admin-Username") String adminUsername,
            @Parameter(description = "投注订单冻结参数", required = true)
            @Valid @RequestBody AdminBetOrderFreezeRequest request) {

        adminBetOrderService.freezeOrder(
                request,
                request.getAdminId(),
                request.getAdminUsername()
        );

        return Result.success();
    }

    /**
     * 结算投注订单。
     *
     * @param request 投注订单结算参数
     * @return 空结果
     */
    @Operation(summary = "结算投注订单", description = "按订单结果结算投注订单并记录结算信息。")
    @PostMapping("/settle")
    public Result<Void> settleOrder(
//            @RequestHeader("X-Admin-Id") Long adminId,
//            @RequestHeader("X-Admin-Username") String adminUsername,
            @Parameter(description = "投注订单结算参数", required = true)
            @Valid @RequestBody AdminBetOrderSettleRequest request) {

        adminBetOrderService.settleOrder(
                request,
                request.getAdminId(),
                request.getAdminUsername()
        );

        return Result.success();
    }
}
