package com.eugene.goalhub.admin.service;

import dto.*;

/**
 * 后台提现订单管理服务。
 */
public interface AdminWithdrawOrderService {

    /**
     * 分页查询提现订单。
     *
     * @param request 提现订单分页查询条件
     * @return 提现订单分页数据
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
     * @param request 提现订单审核参数
     */
    void audit(
            AdminWithdrawOrderAuditRequest request);
}
