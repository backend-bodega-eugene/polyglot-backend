package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminDepositOrderFeignClient;
import com.eugene.goalhub.admin.service.AdminDepositOrderService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台充值订单管理服务实现。
 *
 * <p>当前服务通过 Feign 调用 order-service 的内部后台充值订单接口。</p>
 */
@Service
public class AdminDepositOrderServiceImpl
        implements AdminDepositOrderService {

    /**
     * 后台充值订单远程调用客户端。
     */
    private final AdminDepositOrderFeignClient adminDepositOrderFeignClient;

    /**
     * 创建后台充值订单管理服务实现。
     *
     * @param adminDepositOrderFeignClient 后台充值订单远程调用客户端
     */
    public AdminDepositOrderServiceImpl(
            AdminDepositOrderFeignClient adminDepositOrderFeignClient) {

        this.adminDepositOrderFeignClient =
                adminDepositOrderFeignClient;
    }

    /**
     * 分页查询充值订单。
     *
     * @param request 充值订单分页查询条件
     * @return 充值订单分页数据
     */
    @Override
    public PageResponse<AdminDepositOrderResponse> page(
            AdminDepositOrderPageRequest request) {

        return FeignResultSupport.data(
                adminDepositOrderFeignClient.page(request)
        );
    }

    /**
     * 查询充值订单详情。
     *
     * @param request 充值订单详情查询参数
     * @return 充值订单详情
     */
    @Override
    public AdminDepositOrderResponse detail(
            AdminDepositOrderDetailRequest request) {

        return FeignResultSupport.data(
                adminDepositOrderFeignClient.detail(request)
        );
    }

    /**
     * 审核充值订单。
     *
     * @param request 充值订单审核参数
     */
    @Override
    public void audit(
            AdminDepositOrderAuditRequest request) {

        FeignResultSupport.checkSuccess(
                adminDepositOrderFeignClient.audit(request)
        );
    }
}
