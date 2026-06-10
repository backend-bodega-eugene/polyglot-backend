package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserService;
import dto.VerifyFundPasswordRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "内部用户安全接口", description = "提供内部服务调用的用户安全校验接口")
@RestController
@RequestMapping("/internal/order/user/security")
public class InternalUserSecurityController {

    private final UserService userService;

    public InternalUserSecurityController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/fundpassword/verify")
    public Result<Void> verifyFundPassword(
            @Valid @RequestBody VerifyFundPasswordRequest request) {

        userService.verifyFundPassword(
                request.getUserId(),
                request.getFundPassword()
        );

        return Result.success();
    }
}