package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminBetOrderFeignClient;
import com.eugene.goalhub.admin.service.AdminBetOrderService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台投注订单管理服务实现。
 * <p>
 * 当前服务通过 Feign 调用 order-service 的内部后台投注订单接口。
 */
@Service
public class AdminBetOrderServiceImpl
        implements AdminBetOrderService {

    /**
     * 后台投注订单远程调用客户端。
     */
    private final AdminBetOrderFeignClient adminBetOrderFeignClient;

    /**
     * 创建后台投注订单管理服务实现。
     *
     * @param adminBetOrderFeignClient 后台投注订单远程调用客户端
     */
    public AdminBetOrderServiceImpl(
            AdminBetOrderFeignClient adminBetOrderFeignClient) {

        this.adminBetOrderFeignClient =
                adminBetOrderFeignClient;
    }

    /**
     * 分页查询投注订单。
     *
     * @param request 投注订单分页查询条件
     * @return 投注订单分页数据
     */
    @Override
    public PageResponse<AdminBetOrderResponse> orderPage(
            AdminBetOrderPageRequest request) {

        return FeignResultSupport.data(adminBetOrderFeignClient.orderPage(request));
    }

    /**
     * 分页查询投注订单明细。
     *
     * @param request 投注订单明细分页查询条件
     * @return 投注订单明细分页数据
     */
    @Override
    public PageResponse<AdminBetOrderItemResponse> orderItemPage(
            AdminBetOrderItemPageRequest request) {

        return FeignResultSupport.data(adminBetOrderFeignClient.orderItemPage(request));
    }

    /**
     * 审核投注订单。
     *
     * @param request 投注订单审核参数
     */
    @Override
    public void reviewOrder(
            AdminBetOrderReviewRequest request) {

        FeignResultSupport.checkSuccess(adminBetOrderFeignClient.reviewOrder(request));
    }

    /**
     * 冻结投注订单。
     *
     * @param request 投注订单冻结参数
     */
    @Override
    public void freezeOrder(
            AdminBetOrderFreezeRequest request) {

        FeignResultSupport.checkSuccess(adminBetOrderFeignClient.freezeOrder(request));
    }

    /**
     * 结算投注订单。
     *
     * @param request 投注订单结算参数
     */
    @Override
    public void settleOrder(
            AdminBetOrderSettleRequest request) {

        FeignResultSupport.checkSuccess(adminBetOrderFeignClient.settleOrder(request));
    }
}
