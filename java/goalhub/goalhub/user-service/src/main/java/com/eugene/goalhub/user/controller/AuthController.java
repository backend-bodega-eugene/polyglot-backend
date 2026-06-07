package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.CaptchaService;
import com.eugene.goalhub.user.service.UserService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 用户认证接口。
 *
 * <p>提供图形验证码、用户注册和用户登录接口。</p>
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
     * 创建用户认证接口。
     *
     * @param userService    用户服务
     * @param captchaService 验证码服务
     */
    public AuthController(UserService userService,
                          CaptchaService captchaService) {
        this.userService = userService;
        this.captchaService = captchaService;
    }

    /**
     * 获取图形验证码。
     *
     * @return 图形验证码结果
     */
    @Operation(summary = "获取图形验证码", description = "生成图形验证码图片和验证码标识，用于注册或登录校验。")
    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha() {
        return Result.success(captchaService.generate());
    }

    /**
     * 用户注册。
     *
     * @param request            用户注册参数
     * @param httpServletRequest HTTP 请求对象，用于解析客户端 IP
     * @return 空结果
     */
    @Operation(summary = "用户注册", description = "根据用户名、密码、验证码等注册参数创建前端应用用户。")
    @PostMapping("/register")
    public Result<Void> register(
            @Parameter(description = "用户注册参数", required = true)
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest) {

        String clientIp = getClientIp(httpServletRequest);

        userService.register(request, clientIp);

        return Result.success();
    }

    /**
     * 用户登录。
     *
     * @param request            用户登录参数
     * @param httpServletRequest HTTP 请求对象，用于解析客户端 IP
     * @return 登录结果
     */
    @Operation(summary = "用户登录", description = "校验用户登录信息并返回登录令牌和用户基础信息。")
    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Parameter(description = "用户登录参数", required = true)
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {

        String clientIp = getClientIp(httpServletRequest);

        return Result.success(
                userService.login(request, clientIp)
        );
    }

    /**
     * 修改当前用户登录密码。
     *
     * @param userId  当前登录用户 ID
     * @param request 修改密码参数
     * @return 空结果
     */
    @Operation(summary = "修改密码", description = "用户修改登录密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @Parameter(description = "当前登录用户 ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "修改密码参数", required = true)
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(userId, request);

        return Result.success();
    }

    /**
     * 获取客户端IP。
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP
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
