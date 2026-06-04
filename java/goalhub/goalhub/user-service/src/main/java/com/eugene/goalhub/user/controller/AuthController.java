package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.CaptchaService;
import com.eugene.goalhub.user.service.UserService;
import dto.CaptchaResponse;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 用户认证接口。
 */
@Tag(name = "用户认证", description = "用户注册和登录接口")
@RestController
@RequestMapping("/user")
public class AuthController {

    /**
     * 用户服务。
     */
    private final UserService userService;

    /**
     * 验证码服务。
     */
    private final CaptchaService captchaService;

    /**
     * 构造函数。
     */
    public AuthController(UserService userService,
                          CaptchaService captchaService) {
        this.userService = userService;
        this.captchaService = captchaService;
    }

    /**
     * 获取图形验证码。
     */
    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha() {
        return Result.success(captchaService.generate());
    }

    /**
     * 用户注册。
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(
            @Parameter(description = "用户注册参数", required = true)
            @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest) {

        String clientIp = getClientIp(httpServletRequest);

        userService.register(request, clientIp);

        return Result.success();
    }

    /**
     * 用户登录。
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Parameter(description = "用户登录参数", required = true)
            @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {

        String clientIp = getClientIp(httpServletRequest);

        return Result.success(
                userService.login(request, clientIp)
        );
    }

    /**
     * 获取客户端IP。
     */
    private String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");

        if (ip != null && !ip.isBlank()) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}