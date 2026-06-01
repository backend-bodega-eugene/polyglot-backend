package com.eugene.goalhub.order.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * user-service 内部管理端用户账户 Feign 客户端。
 */
@FeignClient(
        name = "user-service",
        contextId = "OrderUserAccountFeignClient"
)
public interface OrderUserAccountFeignClient {

    /**
     * 增加用户账户余额。
     *
     * @param request 账户余额增加参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/account/addbalance")
    Result<Void> addBalance(
            @RequestBody AdminAccountBalanceChangeRequest request);

    /**
     * 扣减用户账户余额。
     *
     * @param request 账户余额扣减参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/account/subbalance")
    Result<Void> subBalance(
            @RequestBody AdminAccountBalanceChangeRequest request);


}
