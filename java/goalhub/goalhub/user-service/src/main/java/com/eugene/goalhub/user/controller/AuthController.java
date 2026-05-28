package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserService;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

/**
 * 用户认证接口。
 */
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
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    /**
     * 用户登录。
     *
     * @param request 登录参数
     * @return 登录结果，包含 token
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

}
