package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserService;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册。
     *
     * @param request 注册参数
     * @return 空结果
     */
    @Operation(summary = "用户注册", description = "用户使用用户名、密码和昵称注册账号。")
    @PostMapping("/register")
    public Result<Void> register(@Parameter(description = "用户注册参数", required = true)
                                 @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    /**
     * 用户登录。
     *
     * @param request 登录参数
     * @return 登录结果，包含 token
     */
    @Operation(summary = "用户登录", description = "用户使用账号和密码登录，登录成功后返回 token。")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Parameter(description = "用户登录参数", required = true)
                                       @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

}
