package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AdminWithdrawOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 内部后台提现订单管理接口。
 *
 * <p>提供后台提现订单分页查询、详情查询和审核能力。</p>
 */
@Tag(name = "内部后台提现订单管理", description = "提供后台提现订单查询和审核接口")
@RestController
@RequestMapping("/internal/admin/withdraworder")
public class InternalAdminWithdrawOrderController {

    /**
     * 后台提现订单服务。
     */
    private final AdminWithdrawOrderService adminWithdrawOrderService;

    /**
     * 创建内部后台提现订单管理接口实例。
     *
     * @param adminWithdrawOrderService 后台提现订单服务
     */
    public InternalAdminWithdrawOrderController(
            AdminWithdrawOrderService adminWithdrawOrderService) {
        this.adminWithdrawOrderService = adminWithdrawOrderService;
    }

    /**
     * 分页查询提现订单。
     *
     * @param request 提现订单分页查询参数
     * @return 提现订单分页数据
     */
    @Operation(summary = "分页查询提现订单")
    @PostMapping("/page")
    public Result<PageResponse<AdminWithdrawOrderResponse>> page(
            @Parameter(description = "提现订单分页查询参数", required = true)
            @Valid @RequestBody AdminWithdrawOrderPageRequest request) {

        return Result.success(adminWithdrawOrderService.page(request));
    }

    /**
     * 查询提现订单详情。
     *
     * @param request 提现订单详情查询参数
     * @return 提现订单详情
     */
    @Operation(summary = "查询提现订单详情")
    @PostMapping("/detail")
    public Result<AdminWithdrawOrderResponse> detail(
            @Parameter(description = "提现订单详情查询参数", required = true)
            @Valid @RequestBody AdminWithdrawOrderDetailRequest request) {

        return Result.success(adminWithdrawOrderService.detail(request));
    }

    /**
     * 审核提现订单。
     *
     * @param request 提现订单审核参数
     * @return 空结果
     */
    @Operation(summary = "审核提现订单")
    @PostMapping("/audit")
    public Result<Void> audit(
            @Parameter(description = "提现订单审核参数", required = true)
            @Valid @RequestBody AdminWithdrawOrderAuditRequest request) {

        adminWithdrawOrderService.audit(
                request,
                request.getAdminId(),
                request.getAdminUsername()
        );

        return Result.success();
    }
}
