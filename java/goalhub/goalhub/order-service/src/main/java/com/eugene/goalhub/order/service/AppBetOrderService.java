package com.eugene.goalhub.order.service;

import dto.*;

/**
 * 前端投注订单服务。
 *
 * <p>定义前端用户提交投注订单的业务能力。</p>
 */
public interface AppBetOrderService {

    /**
     * 提交投注订单。
     *
     * @param userId  当前登录用户ID
     * @param request 下注请求
     * @return 下单结果
     */
    PlaceBetOrderResponse placeOrder(
            Long userId,
            PlaceBetOrderRequest request);
    PageResponse<AppBetOrderResponse> pageUnsettledOrders(
            Long userId,
            AppBetOrderPageRequest request);

    PageResponse<AppBetOrderResponse> pageSettledOrders(
            Long userId,
            AppBetOrderPageRequest request);
    PageResponse<AppBetOrderResponse> pageMyOrders(
            Long userId,
            AppBetOrderPageRequest request);
}
