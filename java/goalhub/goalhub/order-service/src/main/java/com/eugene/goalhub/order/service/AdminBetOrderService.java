package com.eugene.goalhub.order.service;

import dto.*;

/**
 * 后台投注订单管理服务。
 *
 * <p>定义后台投注订单查询、订单明细查询、审核、冻结和结算能力。</p>
 */
public interface AdminBetOrderService {

    /**
     * 分页查询投注订单。
     *
     * @param request 投注订单分页查询条件
     * @return 投注订单分页结果
     */
    PageResponse<AdminBetOrderResponse> orderPage(
            AdminBetOrderPageRequest request);

    /**
     * 分页查询投注订单明细。
     *
     * @param request 投注订单明细分页查询条件
     * @return 投注订单明细分页结果
     */
    PageResponse<AdminBetOrderItemResponse> orderItemPage(
            AdminBetOrderItemPageRequest request);

    /**
     * 审核投注订单。
     *
     * @param request       投注订单审核参数
     * @param adminId       管理员 ID
     * @param adminUsername 管理员用户名
     */
    void reviewOrder(
            AdminBetOrderReviewRequest request,
            Long adminId,
            String adminUsername);

    /**
     * 冻结投注订单。
     *
     * @param request       投注订单冻结参数
     * @param adminId       管理员 ID
     * @param adminUsername 管理员用户名
     */
    void freezeOrder(
            AdminBetOrderFreezeRequest request,
            Long adminId,
            String adminUsername);

    /**
     * 结算投注订单。
     *
     * @param request       投注订单结算参数
     * @param adminId       管理员 ID
     * @param adminUsername 管理员用户名
     */
    void settleOrder(
            AdminBetOrderSettleRequest request,
            Long adminId,
            String adminUsername);
}
