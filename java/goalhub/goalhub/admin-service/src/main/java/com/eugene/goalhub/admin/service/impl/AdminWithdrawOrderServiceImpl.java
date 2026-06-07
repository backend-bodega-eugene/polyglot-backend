package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminWithdrawOrderFeignClient;
import com.eugene.goalhub.admin.service.AdminWithdrawOrderService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台提现订单管理服务实现。
 */
@Service
public class AdminWithdrawOrderServiceImpl
        implements AdminWithdrawOrderService {

    private final AdminWithdrawOrderFeignClient adminWithdrawOrderFeignClient;

    public AdminWithdrawOrderServiceImpl(
            AdminWithdrawOrderFeignClient adminWithdrawOrderFeignClient) {

        this.adminWithdrawOrderFeignClient =
                adminWithdrawOrderFeignClient;
    }

    @Override
    public PageResponse<AdminWithdrawOrderResponse> page(
            AdminWithdrawOrderPageRequest request) {

        return FeignResultSupport.data(
                adminWithdrawOrderFeignClient.page(request)
        );
    }

    @Override
    public AdminWithdrawOrderResponse detail(
            AdminWithdrawOrderDetailRequest request) {

        return FeignResultSupport.data(
                adminWithdrawOrderFeignClient.detail(request)
        );
    }

    @Override
    public void audit(
            AdminWithdrawOrderAuditRequest request) {

        FeignResultSupport.checkSuccess(
                adminWithdrawOrderFeignClient.audit(request)
        );
    }
}