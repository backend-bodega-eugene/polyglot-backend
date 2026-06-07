package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AdminDepositOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 内部后台充值订单管理接口。
 *
 * <p>提供后台充值订单分页查询、详情查询和审核能力。</p>
 */
@Tag(name = "内部后台充值订单管理", description = "提供后台充值订单查询和审核接口")
@RestController
@RequestMapping("/internal/admin/depositorder")
public class InternalAdminDepositOrderController {

    /**
     * 后台充值订单服务。
     */
    private final AdminDepositOrderService adminDepositOrderService;

    /**
     * 创建内部后台充值订单管理接口实例。
     *
     * @param adminDepositOrderService 后台充值订单服务
     */
    public InternalAdminDepositOrderController(
            AdminDepositOrderService adminDepositOrderService) {
        this.adminDepositOrderService = adminDepositOrderService;
    }

    /**
     * 分页查询充值订单。
     *
     * @param request 充值订单分页查询参数
     * @return 充值订单分页数据
     */
    @Operation(summary = "分页查询充值订单")
    @PostMapping("/page")
    public Result<PageResponse<AdminDepositOrderResponse>> page(
            @Parameter(description = "充值订单分页查询参数", required = true)
            @Valid @RequestBody AdminDepositOrderPageRequest request) {

        return Result.success(adminDepositOrderService.page(request));
    }

    /**
     * 查询充值订单详情。
     *
     * @param request 充值订单详情查询参数
     * @return 充值订单详情
     */
    @Operation(summary = "查询充值订单详情")
    @PostMapping("/detail")
    public Result<AdminDepositOrderResponse> detail(
            @Parameter(description = "充值订单详情查询参数", required = true)
            @Valid @RequestBody AdminDepositOrderDetailRequest request) {

        return Result.success(adminDepositOrderService.detail(request));
    }

    /**
     * 审核充值订单。
     *
     * @param request 充值订单审核参数
     * @return 空结果
     */
    @Operation(summary = "审核充值订单")
    @PostMapping("/audit")
    public Result<Void> audit(
            @Parameter(description = "充值订单审核参数", required = true)
            @Valid @RequestBody AdminDepositOrderAuditRequest request) {

        adminDepositOrderService.audit(
                request,
                request.getAdminId(),
                request.getAdminUsername()
        );

        return Result.success();
    }
}
