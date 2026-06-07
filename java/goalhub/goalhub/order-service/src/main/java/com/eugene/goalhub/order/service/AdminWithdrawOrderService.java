package com.eugene.goalhub.order.service;

import dto.*;

public interface AdminWithdrawOrderService {

    PageResponse<AdminWithdrawOrderResponse> page(
            AdminWithdrawOrderPageRequest request);

    AdminWithdrawOrderResponse detail(
            AdminWithdrawOrderDetailRequest request);

    void audit(
            AdminWithdrawOrderAuditRequest request,
            Long adminId,
            String adminUsername);
}