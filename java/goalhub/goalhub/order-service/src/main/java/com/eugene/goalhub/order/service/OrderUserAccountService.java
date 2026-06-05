package com.eugene.goalhub.order.service;


import dto.*;

/**
 * 后台用户账户管理服务。
 *
 * <p>定义订单服务侧调用用户账户服务进行余额增加和扣减的能力。</p>
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
