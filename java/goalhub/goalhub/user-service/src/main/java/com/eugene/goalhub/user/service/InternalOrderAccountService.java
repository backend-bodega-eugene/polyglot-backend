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

    DeductDefaultAccountResponse deductDefaultUsdt(
            DeductDefaultAccountRequest request);

    void addDefaultUsdt(
            DefaultAccountBalanceChangeRequest request);

    void freezeDefaultUsdt(
            DefaultAccountBalanceChangeRequest request);

    void confirmFrozenDefaultUsdt(
            DefaultAccountBalanceChangeRequest request);

    void unfreezeDefaultUsdt(
            DefaultAccountBalanceChangeRequest request);
}