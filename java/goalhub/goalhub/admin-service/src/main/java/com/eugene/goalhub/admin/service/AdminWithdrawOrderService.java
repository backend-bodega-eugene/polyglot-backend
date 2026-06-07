package com.eugene.goalhub.admin.service;

import dto.*;

/**
 * 后台提现订单管理服务。
 */
public interface AdminWithdrawOrderService {

    PageResponse<AdminWithdrawOrderResponse> page(
            AdminWithdrawOrderPageRequest request);

    AdminWithdrawOrderResponse detail(
            AdminWithdrawOrderDetailRequest request);

    void audit(
            AdminWithdrawOrderAuditRequest request);
}