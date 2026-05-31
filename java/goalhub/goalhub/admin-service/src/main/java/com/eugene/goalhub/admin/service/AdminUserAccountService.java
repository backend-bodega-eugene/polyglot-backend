package com.eugene.goalhub.admin.service;


import dto.*;

/**
 * 后台用户账户管理服务。
 */
public interface AdminUserAccountService {

    /**
     * 分页查询用户账户。
     *
     * @param request 用户账户分页查询条件
     * @return 用户账户分页数据
     */
    PageResponse<AdminUserAccountResponse> accountPage(
            AdminUserAccountPageRequest request);

    /**
     * 分页查询账户流水。
     *
     * @param request 账户流水分页查询条件
     * @return 账户流水分页数据
     */
    PageResponse<AdminAccountTransactionResponse> transactionPage(
            AdminAccountTransactionPageRequest request);

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

    /**
     * 更新用户账户状态。
     *
     * @param request 用户账户状态更新参数
     */
    void updateStatus(AdminAccountStatusUpdateRequest request);
}
