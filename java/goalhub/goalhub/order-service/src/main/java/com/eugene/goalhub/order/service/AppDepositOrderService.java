package com.eugene.goalhub.order.service;

import dto.*;

public interface AppDepositOrderService {

    AppDepositOrderResponse create(
            Long userId,
            AppDepositOrderCreateRequest request);

    PageResponse<AppDepositOrderResponse> page(
            Long userId,
            AppDepositOrderPageRequest request);
}