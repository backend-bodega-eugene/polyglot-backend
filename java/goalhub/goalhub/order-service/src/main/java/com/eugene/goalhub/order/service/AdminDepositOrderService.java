package com.eugene.goalhub.order.service;

import dto.*;

public interface AdminDepositOrderService {

    PageResponse<AdminDepositOrderResponse> page(
            AdminDepositOrderPageRequest request);

    AdminDepositOrderResponse detail(
            AdminDepositOrderDetailRequest request);

    void audit(
            AdminDepositOrderAuditRequest request,
            Long adminId,
            String adminUsername);
}