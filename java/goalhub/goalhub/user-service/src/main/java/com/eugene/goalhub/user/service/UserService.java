package com.eugene.goalhub.user.service;

import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;

/**
 * 用户账号服务。
 */
public interface UserService {

    void register(RegisterRequest request, String clientIp);

    LoginResponse login(LoginRequest request, String clientIp);
}