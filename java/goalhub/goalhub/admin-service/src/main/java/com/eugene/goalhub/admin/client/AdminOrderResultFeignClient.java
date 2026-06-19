package com.eugene.goalhub.admin.client;

import dto.AdminMatchResultJudgeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * order-service 内部后台订单系统预判 Feign 客户端。
 *
 * <p>封装 admin-service 到 order-service 的订单赛果预判内部接口调用。</p>
 */
@FeignClient(
        name = "order-service",
        contextId = "adminOrderResultFeignClient"
)
public interface AdminOrderResultFeignClient {

    /**
     * 根据赛事赛果生成订单系统预判结果。
     *
     * @param request 赛事订单系统预判参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/orderresult/match/judge")
    Result<Void> judgeMatch(
            @RequestBody AdminMatchResultJudgeRequest request);
}
