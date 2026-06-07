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

    /**
     * 分页查询充值订单。
     *
     * @param request 充值订单分页查询条件
     * @return 充值订单分页数据
     */
    @PostMapping("/internal/admin/depositorder/page")
    Result<PageResponse<AdminDepositOrderResponse>> page(
            @RequestBody AdminDepositOrderPageRequest request);

    /**
     * 查询充值订单详情。
     *
     * @param request 充值订单详情查询参数
     * @return 充值订单详情
     */
    @PostMapping("/internal/admin/depositorder/detail")
    Result<AdminDepositOrderResponse> detail(
            @RequestBody AdminDepositOrderDetailRequest request);

    /**
     * 审核充值订单。
     *
     * @param request 充值订单审核参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/depositorder/audit")
    Result<Void> audit(
            @RequestBody AdminDepositOrderAuditRequest request);
}
