package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminWithdrawOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台提现订单管理接口。
 */
@Tag(name = "后台提现订单管理", description = "后台提现订单查询和审核接口")
@RestController
@RequestMapping("/admin/withdraworder")
public class AdminWithdrawOrderController {

    private final AdminWithdrawOrderService adminWithdrawOrderService;

    public AdminWithdrawOrderController(
            AdminWithdrawOrderService adminWithdrawOrderService) {

        this.adminWithdrawOrderService =
                adminWithdrawOrderService;
    }

    @Operation(summary = "分页查询提现订单", description = "后台分页查询提现订单。")
    @PostMapping("/page")
    public Result<PageResponse<AdminWithdrawOrderResponse>> page(
            @Parameter(description = "提现订单分页查询参数", required = true)
            @RequestBody AdminWithdrawOrderPageRequest request) {

        return Result.success(
                adminWithdrawOrderService.page(request)
        );
    }

    @Operation(summary = "查询提现订单详情", description = "根据ID查询提现订单详情。")
    @PostMapping("/detail")
    public Result<AdminWithdrawOrderResponse> detail(
            @Parameter(description = "提现订单详情查询参数", required = true)
            @RequestBody AdminWithdrawOrderDetailRequest request) {

        return Result.success(
                adminWithdrawOrderService.detail(request)
        );
    }

    @Operation(summary = "审核提现订单", description = "审核提现订单，通过或拒绝后由 order-service 负责走账。")
    @PostMapping("/audit")
    public Result<Void> audit(
            @Parameter(description = "当前管理员ID", required = true)
            @RequestHeader("X-Admin-Id") Long adminId,
            @Parameter(description = "当前管理员用户名", required = true)
            @RequestHeader("X-Admin-Username") String username,
            @Parameter(description = "提现订单审核参数", required = true)
            @RequestBody AdminWithdrawOrderAuditRequest request) {

        request.setAdminId(adminId);
        request.setAdminUsername(username);

        adminWithdrawOrderService.audit(request);

        return Result.success();
    }
}