package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AdminDepositOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "内部后台充值订单管理", description = "提供后台充值订单查询和审核接口")
@RestController
@RequestMapping("/internal/admin/depositorder")
public class InternalAdminDepositOrderController {

    private final AdminDepositOrderService adminDepositOrderService;

    public InternalAdminDepositOrderController(
            AdminDepositOrderService adminDepositOrderService) {
        this.adminDepositOrderService = adminDepositOrderService;
    }

    @Operation(summary = "分页查询充值订单")
    @PostMapping("/page")
    public Result<PageResponse<AdminDepositOrderResponse>> page(
            @Valid @RequestBody AdminDepositOrderPageRequest request) {

        return Result.success(adminDepositOrderService.page(request));
    }

    @Operation(summary = "查询充值订单详情")
    @PostMapping("/detail")
    public Result<AdminDepositOrderResponse> detail(
            @Valid @RequestBody AdminDepositOrderDetailRequest request) {

        return Result.success(adminDepositOrderService.detail(request));
    }

    @Operation(summary = "审核充值订单")
    @PostMapping("/audit")
    public Result<Void> audit(
            @Valid @RequestBody AdminDepositOrderAuditRequest request) {

        adminDepositOrderService.audit(
                request,
                request.getAdminId(),
                request.getAdminUsername()
        );

        return Result.success();
    }
}