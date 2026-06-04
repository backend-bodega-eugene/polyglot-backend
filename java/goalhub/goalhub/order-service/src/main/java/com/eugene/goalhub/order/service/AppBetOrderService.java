package com.eugene.goalhub.order.service;

import dto.PlaceBetOrderRequest;
import dto.PlaceBetOrderResponse;

/**
 * 前端投注订单服务。
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
}