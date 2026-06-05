package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.AppUserAccountService;
import dto.AppUserBalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端用户账户接口。
 *
 * <p>提供前端 App 查询当前用户默认账户余额的接口。</p>
 */
@Tag(name = "前端用户账户接口", description = "前端App用户账户余额查询接口")
@RestController
@RequestMapping("/user/account")
public class AppUserAccountController {

    /**
     * 前端用户账户服务。
     */
    private final AppUserAccountService appUserAccountService;

    /**
     * 创建前端用户账户接口。
     *
     * @param appUserAccountService 前端用户账户服务
     */
    public AppUserAccountController(
            AppUserAccountService appUserAccountService) {
        this.appUserAccountService = appUserAccountService;
    }

    /**
     * 查询当前登录用户默认 USDT 账户余额。
     *
     * @param userId 当前登录用户 ID
     * @return 默认 USDT 账户余额
     */
    @Operation(summary = "查询默认USDT账户余额", description = "查询当前登录用户默认 USDT 账户的可用余额。")
    @GetMapping("/me/defaultbalance")
    public Result<AppUserBalanceResponse> getDefaultBalance(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        return Result.success(
                appUserAccountService.getDefaultBalance(userId)
        );
    }
}
