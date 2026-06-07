package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.InternalOrderAccountService;
import dto.DeductDefaultAccountRequest;
import dto.DeductDefaultAccountResponse;
import dto.DefaultAccountBalanceChangeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 订单内部账户接口。
 *
 * <p>提供 order-service 下单、充值、提现时操作用户默认账户的内部接口。</p>
 */
@Tag(name = "订单内部账户接口", description = "供 order-service 操作用户默认账户")
@RestController
@RequestMapping("/internal/order/account")
public class InternalOrderAccountController {

    private final InternalOrderAccountService internalOrderAccountService;

    public InternalOrderAccountController(
            InternalOrderAccountService internalOrderAccountService) {
        this.internalOrderAccountService = internalOrderAccountService;
    }

    @Operation(summary = "扣减默认USDT账户", description = "供 order-service 下单时扣减用户默认 USDT 账户余额。")
    @PostMapping("/deductdefaultusdt")
    public Result<DeductDefaultAccountResponse> deductDefaultUsdt(
            @Parameter(description = "扣减默认USDT账户请求", required = true)
            @Valid @RequestBody DeductDefaultAccountRequest request) {

        return Result.success(
                internalOrderAccountService.deductDefaultUsdt(request)
        );
    }

    @Operation(summary = "增加默认USDT账户余额", description = "供 order-service 充值审核通过时增加用户默认 USDT 账户余额。")
    @PostMapping("/adddefaultusdt")
    public Result<Void> addDefaultUsdt(
            @Parameter(description = "增加默认USDT账户余额请求", required = true)
            @Valid @RequestBody DefaultAccountBalanceChangeRequest request) {

        internalOrderAccountService.addDefaultUsdt(request);

        return Result.success();
    }

    @Operation(summary = "冻结默认USDT账户余额", description = "供 order-service 提现申请时冻结用户默认 USDT 账户余额。")
    @PostMapping("/freezedefaultusdt")
    public Result<Void> freezeDefaultUsdt(
            @Parameter(description = "冻结默认USDT账户余额请求", required = true)
            @Valid @RequestBody DefaultAccountBalanceChangeRequest request) {

        internalOrderAccountService.freezeDefaultUsdt(request);

        return Result.success();
    }

    @Operation(summary = "确认扣减默认USDT冻结余额", description = "供 order-service 提现审核通过时扣减用户默认 USDT 冻结余额。")
    @PostMapping("/confirmfrozendefaultusdt")
    public Result<Void> confirmFrozenDefaultUsdt(
            @Parameter(description = "确认扣减默认USDT冻结余额请求", required = true)
            @Valid @RequestBody DefaultAccountBalanceChangeRequest request) {

        internalOrderAccountService.confirmFrozenDefaultUsdt(request);

        return Result.success();
    }

    @Operation(summary = "解冻默认USDT账户余额", description = "供 order-service 提现审核拒绝时解冻用户默认 USDT 账户余额。")
    @PostMapping("/unfreezedefaultusdt")
    public Result<Void> unfreezeDefaultUsdt(
            @Parameter(description = "解冻默认USDT账户余额请求", required = true)
            @Valid @RequestBody DefaultAccountBalanceChangeRequest request) {

        internalOrderAccountService.unfreezeDefaultUsdt(request);

        return Result.success();
    }
}