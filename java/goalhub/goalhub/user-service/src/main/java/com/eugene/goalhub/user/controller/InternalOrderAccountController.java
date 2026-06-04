package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.InternalOrderAccountService;
import dto.DeductDefaultAccountRequest;
import dto.DeductDefaultAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 订单内部账户接口。
 */
@Tag(name = "订单内部账户接口", description = "供 order-service 下单时扣减用户账户")
@RestController
@RequestMapping("/internal/order/account")
public class InternalOrderAccountController {

    private final InternalOrderAccountService internalOrderAccountService;

    public InternalOrderAccountController(
            InternalOrderAccountService internalOrderAccountService) {
        this.internalOrderAccountService = internalOrderAccountService;
    }

    @Operation(summary = "扣减默认USDT账户")
    @PostMapping("/deductdefaultusdt")
    public Result<DeductDefaultAccountResponse> deductDefaultUsdt(
            @Parameter(description = "扣减默认USDT账户请求", required = true)
            @RequestBody DeductDefaultAccountRequest request) {

        return Result.success(
                internalOrderAccountService.deductDefaultUsdt(request)
        );
    }
}