package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminUserAccountFeignClient;
import com.eugene.goalhub.admin.service.AdminUserAccountService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台用户账户管理服务实现。
 * <p>
 * 当前服务通过 Feign 调用 user-service 的内部用户账户接口。
 */
@Service
public class AdminUserAccountServiceImpl
        implements AdminUserAccountService {

    /**
     * 后台用户账户远程调用客户端。
     */
    private final AdminUserAccountFeignClient
            adminUserAccountFeignClient;

    /**
     * 创建后台用户账户管理服务实现。
     *
     * @param adminUserAccountFeignClient 后台用户账户远程调用客户端
     */
    public AdminUserAccountServiceImpl(
            AdminUserAccountFeignClient adminUserAccountFeignClient) {

        this.adminUserAccountFeignClient =
                adminUserAccountFeignClient;
    }

    /**
     * 分页查询用户账户。
     *
     * @param request 用户账户分页查询条件
     * @return 用户账户分页数据
     */
    @Override
    public PageResponse<AdminUserAccountResponse> accountPage(
            AdminUserAccountPageRequest request) {

        return FeignResultSupport.data(adminUserAccountFeignClient.accountPage(request));
    }

    /**
     * 分页查询账户流水。
     *
     * @param request 账户流水分页查询条件
     * @return 账户流水分页数据
     */
    @Override
    public PageResponse<AdminAccountTransactionResponse>
    transactionPage(
            AdminAccountTransactionPageRequest request) {

        return FeignResultSupport.data(
                adminUserAccountFeignClient.transactionPage(request)
        );
    }

    /**
     * 增加用户账户余额。
     *
     * @param request 账户余额增加参数
     */
    @Override
    public void addBalance(
            AdminAccountBalanceChangeRequest request) {

        FeignResultSupport.checkSuccess(adminUserAccountFeignClient.addBalance(request));
    }

    /**
     * 扣减用户账户余额。
     *
     * @param request 账户余额扣减参数
     */
    @Override
    public void subBalance(
            AdminAccountBalanceChangeRequest request) {

        FeignResultSupport.checkSuccess(adminUserAccountFeignClient.subBalance(request));
    }

    /**
     * 更新用户账户状态。
     *
     * @param request 用户账户状态更新参数
     */
    @Override
    public void updateStatus(
            AdminAccountStatusUpdateRequest request) {

        FeignResultSupport.checkSuccess(adminUserAccountFeignClient.updateStatus(request));
    }
}
