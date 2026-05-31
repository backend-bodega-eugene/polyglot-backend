package com.eugene.goalhub.user.service;

import dto.*;

import java.util.List;

/**
 * 用户账户服务。
 */
public interface UserAccountService {

    /**
     * 查询当前用户账户列表。
     *
     * @param userId 用户ID
     * @return 账户列表
     */
    List<UserAccountResponse> getMyAccounts(Long userId);

    /**
     * 查询当前用户流水分页。
     *
     * @param userId 用户ID
     * @param request 查询条件
     * @return 分页结果
     */
    PageResponse<AccountTransactionResponse> pageMyTransactions(
            Long userId,
            AccountTransactionPageRequest request
    );

    /**
     * 分页查询后台用户账户列表。
     *
     * @param request 后台用户账户分页查询条件
     * @return 后台用户账户分页数据
     */
    PageResponse<AdminUserAccountResponse> adminAccountPage(
            AdminUserAccountPageRequest request
    );

    /**
     * 分页查询后台账户流水列表。
     *
     * @param request 后台账户流水分页查询条件
     * @return 后台账户流水分页数据
     */
    PageResponse<AdminAccountTransactionResponse> adminTransactionPage(
            AdminAccountTransactionPageRequest request
    );

    /**
     * 后台增加账户余额。
     *
     * @param request 账户余额增加参数
     */
    void adminAddBalance(AdminAccountBalanceChangeRequest request);

    /**
     * 后台扣减账户余额。
     *
     * @param request 账户余额扣减参数
     */
    void adminSubBalance(AdminAccountBalanceChangeRequest request);

    /**
     * 后台更新账户状态。
     *
     * @param request 账户状态更新参数
     */
    void adminUpdateStatus(AdminAccountStatusUpdateRequest request);
}
