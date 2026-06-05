package com.eugene.goalhub.user.service;

import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;

/**
 * 用户账号服务。
 *
 * <p>定义前端用户注册和登录能力。</p>
 */
public interface UserService {

    /**
     * 注册前端应用用户。
     *
     * @param request  用户注册参数
     * @param clientIp 客户端 IP
     */
    void register(RegisterRequest request, String clientIp);

    /**
     * 前端应用用户登录。
     *
     * @param request  用户登录参数
     * @param clientIp 客户端 IP
     * @return 登录结果
     */
    LoginResponse login(LoginRequest request, String clientIp);
}
