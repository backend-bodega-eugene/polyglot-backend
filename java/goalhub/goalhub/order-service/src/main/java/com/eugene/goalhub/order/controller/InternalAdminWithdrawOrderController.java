package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AdminWithdrawOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "内部后台提现订单管理", description = "提供后台提现订单查询和审核接口")
@RestController
@RequestMapping("/internal/admin/withdraworder")
public class InternalAdminWithdrawOrderController {

    private final AdminWithdrawOrderService adminWithdrawOrderService;

    public InternalAdminWithdrawOrderController(
            AdminWithdrawOrderService adminWithdrawOrderService) {
        this.adminWithdrawOrderService = adminWithdrawOrderService;
    }

    @Operation(summary = "分页查询提现订单")
    @PostMapping("/page")
    public Result<PageResponse<AdminWithdrawOrderResponse>> page(
            @Valid @RequestBody AdminWithdrawOrderPageRequest request) {

        return Result.success(adminWithdrawOrderService.page(request));
    }

    @Operation(summary = "查询提现订单详情")
    @PostMapping("/detail")
    public Result<AdminWithdrawOrderResponse> detail(
            @Valid @RequestBody AdminWithdrawOrderDetailRequest request) {

        return Result.success(adminWithdrawOrderService.detail(request));
    }

    @Operation(summary = "审核提现订单")
    @PostMapping("/audit")
    public Result<Void> audit(
            @Valid @RequestBody AdminWithdrawOrderAuditRequest request) {

        adminWithdrawOrderService.audit(
                request,
                request.getAdminId(),
                request.getAdminUsername()
        );

        return Result.success();
    }
}