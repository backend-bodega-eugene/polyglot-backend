package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminDepositOrderFeignClient;
import com.eugene.goalhub.admin.service.AdminDepositOrderService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台充值订单管理服务实现。
 */
@Service
public class AdminDepositOrderServiceImpl
        implements AdminDepositOrderService {

    private final AdminDepositOrderFeignClient adminDepositOrderFeignClient;

    public AdminDepositOrderServiceImpl(
            AdminDepositOrderFeignClient adminDepositOrderFeignClient) {

        this.adminDepositOrderFeignClient =
                adminDepositOrderFeignClient;
    }

    @Override
    public PageResponse<AdminDepositOrderResponse> page(
            AdminDepositOrderPageRequest request) {

        return FeignResultSupport.data(
                adminDepositOrderFeignClient.page(request)
        );
    }

    @Override
    public AdminDepositOrderResponse detail(
            AdminDepositOrderDetailRequest request) {

        return FeignResultSupport.data(
                adminDepositOrderFeignClient.detail(request)
        );
    }

    @Override
    public void audit(
            AdminDepositOrderAuditRequest request) {

        FeignResultSupport.checkSuccess(
                adminDepositOrderFeignClient.audit(request)
        );
    }
}