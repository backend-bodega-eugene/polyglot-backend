package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * order-service 内部后台充值订单 Feign 客户端。
 */
@FeignClient(
        name = "order-service",
        contextId = "adminDepositOrderFeignClient"
)
public interface AdminDepositOrderFeignClient {

    @PostMapping("/internal/admin/depositorder/page")
    Result<PageResponse<AdminDepositOrderResponse>> page(
            @RequestBody AdminDepositOrderPageRequest request);

    @PostMapping("/internal/admin/depositorder/detail")
    Result<AdminDepositOrderResponse> detail(
            @RequestBody AdminDepositOrderDetailRequest request);

    @PostMapping("/internal/admin/depositorder/audit")
    Result<Void> audit(
            @RequestBody AdminDepositOrderAuditRequest request);
}