package com.eugene.goalhub.user.service;

import dto.AppUserBalanceResponse;

/**
 * 前端用户账户服务。
 *
 * <p>定义前端用户查询默认账户余额的业务能力。</p>
 */
public interface AppUserAccountService {

    /**
     * 查询当前用户默认 USDT 账户余额。
     *
     * @param userId 当前登录用户 ID
     * @return 默认账户余额
     */
    AppUserBalanceResponse getDefaultBalance(
            Long userId);
}
