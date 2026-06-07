package com.eugene.goalhub.order.service;

import dto.*;

/**
 * 后台充值订单服务。
 *
 * <p>提供后台充值订单分页查询、详情查询和审核能力。</p>
 */
public interface AdminDepositOrderService {

    /**
     * 分页查询后台充值订单。
     *
     * @param request 充值订单分页查询参数
     * @return 充值订单分页结果
     */
    PageResponse<AdminDepositOrderResponse> page(
            AdminDepositOrderPageRequest request);

    /**
     * 查询充值订单详情。
     *
     * @param request 充值订单详情查询参数
     * @return 充值订单详情
     */
    AdminDepositOrderResponse detail(
            AdminDepositOrderDetailRequest request);

    /**
     * 审核充值订单。
     *
     * @param request       充值订单审核参数
     * @param adminId       审核管理员 ID
     * @param adminUsername 审核管理员用户名
     */
    void audit(
            AdminDepositOrderAuditRequest request,
            Long adminId,
            String adminUsername);
}
