package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminWithdrawOrderFeignClient;
import com.eugene.goalhub.admin.service.AdminWithdrawOrderService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台提现订单管理服务实现。
 *
 * <p>当前服务通过 Feign 调用 order-service 的内部后台提现订单接口。</p>
 */
@Service
public class AdminWithdrawOrderServiceImpl
        implements AdminWithdrawOrderService {

    /**
     * 后台提现订单远程调用客户端。
     */
    private final AdminWithdrawOrderFeignClient adminWithdrawOrderFeignClient;

    /**
     * 创建后台提现订单管理服务实现。
     *
     * @param adminWithdrawOrderFeignClient 后台提现订单远程调用客户端
     */
    public AdminWithdrawOrderServiceImpl(
            AdminWithdrawOrderFeignClient adminWithdrawOrderFeignClient) {

        this.adminWithdrawOrderFeignClient =
                adminWithdrawOrderFeignClient;
    }

    /**
     * 分页查询提现订单。
     *
     * @param request 提现订单分页查询条件
     * @return 提现订单分页数据
     */
    @Override
    public PageResponse<AdminWithdrawOrderResponse> page(
            AdminWithdrawOrderPageRequest request) {

        return FeignResultSupport.data(
                adminWithdrawOrderFeignClient.page(request)
        );
    }

    /**
     * 查询提现订单详情。
     *
     * @param request 提现订单详情查询参数
     * @return 提现订单详情
     */
    @Override
    public AdminWithdrawOrderResponse detail(
            AdminWithdrawOrderDetailRequest request) {

        return FeignResultSupport.data(
                adminWithdrawOrderFeignClient.detail(request)
        );
    }

    /**
     * 审核提现订单。
     *
     * @param request 提现订单审核参数
     */
    @Override
    public void audit(
            AdminWithdrawOrderAuditRequest request) {

        FeignResultSupport.checkSuccess(
                adminWithdrawOrderFeignClient.audit(request)
        );
    }
}
