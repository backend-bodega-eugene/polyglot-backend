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

    /**
     * 分页查询当前用户未结算投注订单。
     *
     * @param userId  当前登录用户 ID
     * @param request 投注订单分页查询参数
     * @return 未结算投注订单分页结果
     */
    PageResponse<AppBetOrderResponse> pageUnsettledOrders(
            Long userId,
            AppBetOrderPageRequest request);

    /**
     * 分页查询当前用户已结算投注订单。
     *
     * @param userId  当前登录用户 ID
     * @param request 投注订单分页查询参数
     * @return 已结算投注订单分页结果
     */
    PageResponse<AppBetOrderResponse> pageSettledOrders(
            Long userId,
            AppBetOrderPageRequest request);

    /**
     * 分页查询当前用户全部投注订单。
     *
     * @param userId  当前登录用户 ID
     * @param request 投注订单分页查询参数
     * @return 投注订单分页结果
     */
    PageResponse<AppBetOrderResponse> pageMyOrders(
            Long userId,
            AppBetOrderPageRequest request);

    /**
     * 提交冠军投注订单。
     *
     * @param userId  当前登录用户 ID
     * @param request 冠军下注请求
     * @return 下单结果
     */
    PlaceBetOrderResponse placeChampionOrder(
            Long userId,
            PlaceChampionBetOrderRequest request);
}
