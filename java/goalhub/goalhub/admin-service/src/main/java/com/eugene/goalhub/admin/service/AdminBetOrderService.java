package com.eugene.goalhub.admin.service;

import dto.*;

/**
 * 后台投注订单管理服务。
 */
public interface AdminBetOrderService {

    /**
     * 分页查询投注订单。
     *
     * @param request 投注订单分页查询条件
     * @return 投注订单分页数据
     */
    PageResponse<AdminBetOrderResponse> orderPage(
            AdminBetOrderPageRequest request);

    /**
     * 分页查询投注订单明细。
     *
     * @param request 投注订单明细分页查询条件
     * @return 投注订单明细分页数据
     */
    PageResponse<AdminBetOrderItemResponse> orderItemPage(
            AdminBetOrderItemPageRequest request);

    /**
     * 审核投注订单。
     *
     * @param request 投注订单审核参数
     */
    void reviewOrder(AdminBetOrderReviewRequest request);

    /**
     * 冻结投注订单。
     *
     * @param request 投注订单冻结参数
     */
    void freezeOrder(AdminBetOrderFreezeRequest request);

    /**
     * 结算投注订单。
     *
     * @param request 投注订单结算参数
     */
    void settleOrder(AdminBetOrderSettleRequest request);
}
