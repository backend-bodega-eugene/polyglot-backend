package com.eugene.goalhub.user.service;

import dto.DeductDefaultAccountRequest;
import dto.DeductDefaultAccountResponse;

/**
 * 订单内部账户服务。
 */
public interface InternalOrderAccountService {

    /**
     * 扣减默认 USDT 账户余额。
     *
     * @param request 扣减默认账户请求
     * @return 扣减结果
     */
    DeductDefaultAccountResponse deductDefaultUsdt(
            DeductDefaultAccountRequest request);
}