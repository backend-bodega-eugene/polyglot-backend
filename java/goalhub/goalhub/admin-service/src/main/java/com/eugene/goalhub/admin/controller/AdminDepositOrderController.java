package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminDepositOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台充值订单管理接口。
 */
@Tag(name = "后台充值订单管理", description = "后台充值订单查询和审核接口")
@RestController
@RequestMapping("/admin/depositorder")
public class AdminDepositOrderController {

    private final AdminDepositOrderService adminDepositOrderService;

    public AdminDepositOrderController(
            AdminDepositOrderService adminDepositOrderService) {

        this.adminDepositOrderService =
                adminDepositOrderService;
    }

    @Operation(summary = "分页查询充值订单", description = "后台分页查询充值订单。")
    @PostMapping("/page")
    public Result<PageResponse<AdminDepositOrderResponse>> page(
            @Parameter(description = "充值订单分页查询参数", required = true)
            @RequestBody AdminDepositOrderPageRequest request) {

        return Result.success(
                adminDepositOrderService.page(request)
        );
    }

    @Operation(summary = "查询充值订单详情", description = "根据ID查询充值订单详情。")
    @PostMapping("/detail")
    public Result<AdminDepositOrderResponse> detail(
            @Parameter(description = "充值订单详情查询参数", required = true)
            @RequestBody AdminDepositOrderDetailRequest request) {

        return Result.success(
                adminDepositOrderService.detail(request)
        );
    }

    @Operation(summary = "审核充值订单", description = "审核充值订单，通过后由 order-service 负责走账。")
    @PostMapping("/audit")
    public Result<Void> audit(
            @Parameter(description = "当前管理员ID", required = true)
            @RequestHeader("X-Admin-Id") Long adminId,
            @Parameter(description = "当前管理员用户名", required = true)
            @RequestHeader("X-Admin-Username") String username,
            @Parameter(description = "充值订单审核参数", required = true)
            @RequestBody AdminDepositOrderAuditRequest request) {

        request.setAdminId(adminId);
        request.setAdminUsername(username);

        adminDepositOrderService.audit(request);

        return Result.success();
    }
}