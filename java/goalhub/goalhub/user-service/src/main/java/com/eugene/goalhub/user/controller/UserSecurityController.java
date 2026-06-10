package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserService;
import dto.ChangeFundPasswordRequest;
import dto.SetFundPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "用户安全", description = "资金密码设置和修改")
@RestController
@RequestMapping("/user/security")
public class UserSecurityController {

    private final UserService userService;

    public UserSecurityController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "设置资金密码")
    @PostMapping("/fundpassword/set")
    public Result<Void> setFundPassword(
            @Parameter(description = "当前登录用户 ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SetFundPasswordRequest request) {

        userService.setFundPassword(userId, request);
        return Result.success();
    }

    @Operation(summary = "修改资金密码")
    @PostMapping("/fundpassword/change")
    public Result<Void> changeFundPassword(
            @Parameter(description = "当前登录用户 ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ChangeFundPasswordRequest request) {

        userService.changeFundPassword(userId, request);
        return Result.success();
    }
}