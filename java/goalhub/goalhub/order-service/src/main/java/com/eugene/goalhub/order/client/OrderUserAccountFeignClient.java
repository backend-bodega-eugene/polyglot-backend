package com.eugene.goalhub.order.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * user-service 内部管理端用户账户 Feign 客户端。
 */
@FeignClient(
        name = "user-service",
        contextId = "OrderUserAccountFeignClient"
)
public interface OrderUserAccountFeignClient {

    /**
     * 增加用户账户余额。
     *
     * @param request 账户余额增加参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/account/addbalance")
    Result<Void> addBalance(
            @RequestBody AdminAccountBalanceChangeRequest request);

    /**
     * 扣减用户账户余额。
     *
     * @param request 账户余额扣减参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/account/subbalance")
    Result<Void> subBalance(
            @RequestBody AdminAccountBalanceChangeRequest request);

    /**
     * 扣减用户默认 USDT 账户余额。
     *
     * @param request 默认账户扣款参数
     * @return 默认账户扣款结果
     */
    @PostMapping("/internal/order/account/deductdefaultusdt")
    Result<DeductDefaultAccountResponse> deductDefaultUsdt(
            @RequestBody DeductDefaultAccountRequest request);
    @PostMapping("/internal/order/account/adddefaultusdt")
    Result<Void> addDefaultUsdt(
            @RequestBody DefaultAccountBalanceChangeRequest request);

    @PostMapping("/internal/order/account/freezedefaultusdt")
    Result<Void> freezeDefaultUsdt(
            @RequestBody DefaultAccountBalanceChangeRequest request);

    @PostMapping("/internal/order/account/confirmfrozendefaultusdt")
    Result<Void> confirmFrozenDefaultUsdt(
            @RequestBody DefaultAccountBalanceChangeRequest request);

    @PostMapping("/internal/order/account/unfreezedefaultusdt")
    Result<Void> unfreezeDefaultUsdt(
            @RequestBody DefaultAccountBalanceChangeRequest request);
}
