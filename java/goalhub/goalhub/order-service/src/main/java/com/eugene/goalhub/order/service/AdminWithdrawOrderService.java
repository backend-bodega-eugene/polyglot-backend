package com.eugene.goalhub.order.service;

import dto.*;

/**
 * 后台提现订单服务。
 *
 * <p>提供后台提现订单分页查询、详情查询和审核能力。</p>
 */
public interface AdminWithdrawOrderService {

    /**
     * 分页查询后台提现订单。
     *
     * @param request 提现订单分页查询参数
     * @return 提现订单分页结果
     */
    PageResponse<AdminWithdrawOrderResponse> page(
            AdminWithdrawOrderPageRequest request);

    /**
     * 查询提现订单详情。
     *
     * @param request 提现订单详情查询参数
     * @return 提现订单详情
     */
    AdminWithdrawOrderResponse detail(
            AdminWithdrawOrderDetailRequest request);

    /**
     * 审核提现订单。
     *
     * @param request       提现订单审核参数
     * @param adminId       审核管理员 ID
     * @param adminUsername 审核管理员用户名
     */
    void audit(
            AdminWithdrawOrderAuditRequest request,
            Long adminId,
            String adminUsername);
}
