package com.eugene.goalhub.admin.service;

import dto.*;

/**
 * 后台充值订单管理服务。
 */
public interface AdminDepositOrderService {

    /**
     * 分页查询充值订单。
     *
     * @param request 充值订单分页查询条件
     * @return 充值订单分页数据
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
     * @param request 充值订单审核参数
     */
    void audit(
            AdminDepositOrderAuditRequest request);
}
