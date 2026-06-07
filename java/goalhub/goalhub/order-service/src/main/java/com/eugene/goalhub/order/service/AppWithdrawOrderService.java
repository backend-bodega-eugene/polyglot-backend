package com.eugene.goalhub.order.service;

import dto.*;

/**
 * 前端提现订单服务。
 *
 * <p>提供用户提交提现申请和查询个人提现订单的能力。</p>
 */
public interface AppWithdrawOrderService {

    /**
     * 创建提现订单。
     *
     * @param userId  用户 ID
     * @param request 提现申请参数
     * @return 创建后的提现订单
     */
    AppWithdrawOrderResponse create(
            Long userId,
            AppWithdrawOrderCreateRequest request);

    /**
     * 分页查询当前用户提现订单。
     *
     * @param userId  用户 ID
     * @param request 提现订单分页查询参数
     * @return 提现订单分页结果
     */
    PageResponse<AppWithdrawOrderResponse> page(
            Long userId,
            AppWithdrawOrderPageRequest request);
}
