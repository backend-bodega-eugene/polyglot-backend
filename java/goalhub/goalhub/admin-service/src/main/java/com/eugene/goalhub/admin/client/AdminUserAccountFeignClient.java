package com.eugene.goalhub.admin.client;

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
        contextId = "adminUserAccountFeignClient"
)
public interface AdminUserAccountFeignClient {

    /**
     * 分页查询用户账户。
     *
     * @param request 用户账户分页查询条件
     * @return 用户账户分页数据
     */
    @PostMapping("/internal/admin/account/page")
    Result<PageResponse<AdminUserAccountResponse>> accountPage(
            @RequestBody AdminUserAccountPageRequest request);

    /**
     * 分页查询账户流水。
     *
     * @param request 账户流水分页查询条件
     * @return 账户流水分页数据
     */
    @PostMapping("/internal/admin/account/transaction/page")
    Result<PageResponse<AdminAccountTransactionResponse>> transactionPage(
            @RequestBody AdminAccountTransactionPageRequest request);

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
     * 更新用户账户状态。
     *
     * @param request 用户账户状态更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/account/updatestatus")
    Result<Void> updateStatus(
            @RequestBody AdminAccountStatusUpdateRequest request);
}
