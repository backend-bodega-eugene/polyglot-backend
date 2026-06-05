package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.InternalOrderAccountService;
import dto.DeductDefaultAccountRequest;
import dto.DeductDefaultAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 订单内部账户接口。
 *
 * <p>提供 order-service 下单时扣减用户默认账户余额的内部接口。</p>
 */
@Tag(name = "订单内部账户接口", description = "供 order-service 下单时扣减用户账户")
@RestController
@RequestMapping("/internal/order/account")
public class InternalOrderAccountController {

    /**
     * 订单内部账户服务。
     */
    private final InternalOrderAccountService internalOrderAccountService;

    /**
     * 创建订单内部账户接口。
     *
     * @param internalOrderAccountService 订单内部账户服务
     */
    public InternalOrderAccountController(
            InternalOrderAccountService internalOrderAccountService) {
        this.internalOrderAccountService = internalOrderAccountService;
    }

    /**
     * 扣减默认 USDT 账户余额。
     *
     * @param request 默认 USDT 账户扣减参数
     * @return 默认账户扣减结果
     */
    @Operation(summary = "扣减默认USDT账户", description = "供 order-service 下单时扣减用户默认 USDT 账户余额。")
    @PostMapping("/deductdefaultusdt")
    public Result<DeductDefaultAccountResponse> deductDefaultUsdt(
            @Parameter(description = "扣减默认USDT账户请求", required = true)
            @Valid @RequestBody DeductDefaultAccountRequest request) {

        return Result.success(
                internalOrderAccountService.deductDefaultUsdt(request)
        );
    }
}
