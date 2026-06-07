package com.eugene.goalhub.order.service;

import dto.*;

/**
 * 前端充值订单服务。
 *
 * <p>提供用户提交充值申请和查询个人充值订单的能力。</p>
 */
public interface AppDepositOrderService {

    /**
     * 创建充值订单。
     *
     * @param userId  用户 ID
     * @param request 充值申请参数
     * @return 创建后的充值订单
     */
    AppDepositOrderResponse create(
            Long userId,
            AppDepositOrderCreateRequest request);

    /**
     * 分页查询当前用户充值订单。
     *
     * @param userId  用户 ID
     * @param request 充值订单分页查询参数
     * @return 充值订单分页结果
     */
    PageResponse<AppDepositOrderResponse> page(
            Long userId,
            AppDepositOrderPageRequest request);
}
