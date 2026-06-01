package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * order-service 内部后台投注订单 Feign 客户端。
 */
@FeignClient(
        name = "order-service",
        contextId = "adminBetOrderFeignClient"
)
public interface AdminBetOrderFeignClient {

    /**
     * 分页查询投注订单。
     *
     * @param request 投注订单分页查询条件
     * @return 投注订单分页数据
     */
    @PostMapping("/internal/admin/order/page")
    Result<PageResponse<AdminBetOrderResponse>> orderPage(
            @RequestBody AdminBetOrderPageRequest request);

    /**
     * 分页查询投注订单明细。
     *
     * @param request 投注订单明细分页查询条件
     * @return 投注订单明细分页数据
     */
    @PostMapping("/internal/admin/order/item/page")
    Result<PageResponse<AdminBetOrderItemResponse>> orderItemPage(
            @RequestBody AdminBetOrderItemPageRequest request);

    /**
     * 审核投注订单。
     *
     * @param request 投注订单审核参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/order/review")
    Result<Void> reviewOrder(
            @RequestBody AdminBetOrderReviewRequest request);

    /**
     * 冻结投注订单。
     *
     * @param request 投注订单冻结参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/order/freeze")
    Result<Void> freezeOrder(
            @RequestBody AdminBetOrderFreezeRequest request);

    /**
     * 结算投注订单。
     *
     * @param request 投注订单结算参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/order/settle")
    Result<Void> settleOrder(
            @RequestBody AdminBetOrderSettleRequest request);
}
