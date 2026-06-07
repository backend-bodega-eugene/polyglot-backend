package com.eugene.goalhub.order.service;

import dto.*;

public interface AppWithdrawOrderService {

    AppWithdrawOrderResponse create(
            Long userId,
            AppWithdrawOrderCreateRequest request);

    PageResponse<AppWithdrawOrderResponse> page(
            Long userId,
            AppWithdrawOrderPageRequest request);
}