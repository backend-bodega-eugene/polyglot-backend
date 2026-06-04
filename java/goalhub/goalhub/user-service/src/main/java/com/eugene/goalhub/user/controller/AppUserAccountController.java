package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.AppUserAccountService;
import dto.AppUserBalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "前端用户账户接口", description = "前端App用户账户余额查询接口")
@RestController
@RequestMapping("/user/account")
public class AppUserAccountController {

    private final AppUserAccountService appUserAccountService;

    public AppUserAccountController(
            AppUserAccountService appUserAccountService) {
        this.appUserAccountService = appUserAccountService;
    }

    @Operation(summary = "查询默认USDT账户余额")
    @GetMapping("/me/defaultbalance")
    public Result<AppUserBalanceResponse> getDefaultBalance(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        return Result.success(
                appUserAccountService.getDefaultBalance(userId)
        );
    }
}