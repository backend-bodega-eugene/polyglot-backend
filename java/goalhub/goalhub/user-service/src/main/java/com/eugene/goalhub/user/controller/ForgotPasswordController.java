package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.ForgotPasswordService;
import dto.ForgotPasswordResetRequest;
import dto.ForgotPasswordSendCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "找回密码", description = "通过绑定邮箱找回登录密码")
@RestController
@RequestMapping("/user/forgotpassword")
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }

    @Operation(summary = "发送找回密码邮箱验证码")
    @PostMapping("/sendcode")
    public Result<Void> sendCode(
            @Parameter(description = "发送验证码请求", required = true)
            @Valid @RequestBody ForgotPasswordSendCodeRequest request) {

        forgotPasswordService.sendCode(request);
        return Result.success();
    }

    @Operation(summary = "通过邮箱验证码重置密码")
    @PostMapping("/reset")
    public Result<Void> reset(
            @Parameter(description = "重置密码请求", required = true)
            @Valid @RequestBody ForgotPasswordResetRequest request) {

        forgotPasswordService.reset(request);
        return Result.success();
    }
}