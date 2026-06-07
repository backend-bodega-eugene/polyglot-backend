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

    /**
     * 分页查询提现订单。
     *
     * @param request 提现订单分页查询条件
     * @return 提现订单分页数据
     */
    @PostMapping("/internal/admin/withdraworder/page")
    Result<PageResponse<AdminWithdrawOrderResponse>> page(
            @RequestBody AdminWithdrawOrderPageRequest request);

    /**
     * 查询提现订单详情。
     *
     * @param request 提现订单详情查询参数
     * @return 提现订单详情
     */
    @PostMapping("/internal/admin/withdraworder/detail")
    Result<AdminWithdrawOrderResponse> detail(
            @RequestBody AdminWithdrawOrderDetailRequest request);

    /**
     * 审核提现订单。
     *
     * @param request 提现订单审核参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/withdraworder/audit")
    Result<Void> audit(
            @RequestBody AdminWithdrawOrderAuditRequest request);
}
