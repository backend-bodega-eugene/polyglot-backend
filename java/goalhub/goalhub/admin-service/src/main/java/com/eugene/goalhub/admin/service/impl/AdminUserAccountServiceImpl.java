package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminUserAccountFeignClient;
import com.eugene.goalhub.admin.service.AdminUserAccountService;
import dto.*;
import org.springframework.stereotype.Service;
import response.Result;

@Service
public class AdminUserAccountServiceImpl
        implements AdminUserAccountService {

    private final AdminUserAccountFeignClient
            adminUserAccountFeignClient;

    public AdminUserAccountServiceImpl(
            AdminUserAccountFeignClient adminUserAccountFeignClient) {

        this.adminUserAccountFeignClient =
                adminUserAccountFeignClient;
    }

    @Override
    public PageResponse<AdminUserAccountResponse> accountPage(
            AdminUserAccountPageRequest request) {

        Result<PageResponse<AdminUserAccountResponse>> result =
                adminUserAccountFeignClient.accountPage(request);

        return result.getData();
    }

    @Override
    public PageResponse<AdminAccountTransactionResponse>
    transactionPage(
            AdminAccountTransactionPageRequest request) {

        Result<PageResponse<AdminAccountTransactionResponse>> result =
                adminUserAccountFeignClient.transactionPage(
                        request
                );

        return result.getData();
    }
    @Override
    public void addBalance(
            AdminAccountBalanceChangeRequest request) {

        adminUserAccountFeignClient.addBalance(request);
    }

    @Override
    public void subBalance(
            AdminAccountBalanceChangeRequest request) {

        adminUserAccountFeignClient.subBalance(request);
    }

    @Override
    public void updateStatus(
            AdminAccountStatusUpdateRequest request) {

        adminUserAccountFeignClient.updateStatus(request);
    }
}