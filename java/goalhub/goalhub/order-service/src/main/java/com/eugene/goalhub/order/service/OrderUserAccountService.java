package com.eugene.goalhub.order.service;


import dto.*;

/**
 * 后台用户账户管理服务。
 */
public interface OrderUserAccountService {

    /**
     * 增加用户账户余额。
     *
     * @param request 账户余额增加参数
     */
    void addBalance(AdminAccountBalanceChangeRequest request);

    /**
     * 扣减用户账户余额。
     *
     * @param request 账户余额扣减参数
     */
    void subBalance(AdminAccountBalanceChangeRequest request);

}
