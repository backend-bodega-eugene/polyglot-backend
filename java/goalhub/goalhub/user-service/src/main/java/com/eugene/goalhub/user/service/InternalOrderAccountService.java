package com.eugene.goalhub.user.service;

import dto.DeductDefaultAccountRequest;
import dto.DeductDefaultAccountResponse;
import dto.DefaultAccountBalanceChangeRequest;

/**
 * 订单内部账户服务。
 *
 * <p>定义 order-service 操作用户默认账户的内部业务能力。</p>
 */
public interface InternalOrderAccountService {

    /**
     * 扣减用户默认 USDT 账户余额。
     *
     * @param request 扣减默认账户请求
     * @return 扣减后的账户余额信息
     */
    DeductDefaultAccountResponse deductDefaultUsdt(
            DeductDefaultAccountRequest request);

    /**
     * 增加用户默认 USDT 账户余额。
     *
     * @param request 默认账户余额变更请求
     */
    void addDefaultUsdt(
            DefaultAccountBalanceChangeRequest request);

    /**
     * 冻结用户默认 USDT 账户余额。
     *
     * @param request 默认账户余额变更请求
     */
    void freezeDefaultUsdt(
            DefaultAccountBalanceChangeRequest request);

    /**
     * 确认扣减用户默认 USDT 冻结余额。
     *
     * @param request 默认账户余额变更请求
     */
    void confirmFrozenDefaultUsdt(
            DefaultAccountBalanceChangeRequest request);

    /**
     * 解冻用户默认 USDT 账户余额。
     *
     * @param request 默认账户余额变更请求
     */
    void unfreezeDefaultUsdt(
            DefaultAccountBalanceChangeRequest request);
}
