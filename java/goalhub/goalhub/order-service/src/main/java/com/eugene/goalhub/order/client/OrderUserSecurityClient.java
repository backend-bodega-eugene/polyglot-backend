package com.eugene.goalhub.order.client;

import dto.VerifyFundPasswordRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

@FeignClient(name = "user-service", contextId = "orderUserSecurityClient")
public interface OrderUserSecurityClient {

    @PostMapping("/internal/order/user/security/fundpassword/verify")
    Result<Void> verifyFundPassword(@RequestBody VerifyFundPasswordRequest request);
}