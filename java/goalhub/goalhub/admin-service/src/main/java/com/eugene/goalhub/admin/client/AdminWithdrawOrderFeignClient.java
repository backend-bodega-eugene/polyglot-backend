package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * order-service 内部后台提现订单 Feign 客户端。
 */
@FeignClient(
        name = "order-service",
        contextId = "adminWithdrawOrderFeignClient"
)
public interface AdminWithdrawOrderFeignClient {

    @PostMapping("/internal/admin/withdraworder/page")
    Result<PageResponse<AdminWithdrawOrderResponse>> page(
            @RequestBody AdminWithdrawOrderPageRequest request);

    @PostMapping("/internal/admin/withdraworder/detail")
    Result<AdminWithdrawOrderResponse> detail(
            @RequestBody AdminWithdrawOrderDetailRequest request);

    @PostMapping("/internal/admin/withdraworder/audit")
    Result<Void> audit(
            @RequestBody AdminWithdrawOrderAuditRequest request);
}