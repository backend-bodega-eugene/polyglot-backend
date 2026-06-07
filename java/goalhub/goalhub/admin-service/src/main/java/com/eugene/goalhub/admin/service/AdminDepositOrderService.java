package com.eugene.goalhub.admin.service;

import dto.*;

/**
 * 后台充值订单管理服务。
 */
public interface AdminDepositOrderService {

    PageResponse<AdminDepositOrderResponse> page(
            AdminDepositOrderPageRequest request);

    AdminDepositOrderResponse detail(
            AdminDepositOrderDetailRequest request);

    void audit(
            AdminDepositOrderAuditRequest request);
}