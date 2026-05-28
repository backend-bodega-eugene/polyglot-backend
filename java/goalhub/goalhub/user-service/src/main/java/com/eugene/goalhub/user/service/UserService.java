package com.eugene.goalhub.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eugene.goalhub.user.entity.UserEntity;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;

/**
 * 用户账号服务。
 */
public interface UserService {

    /**
     * 注册用户。
     *
     * @param request 注册参数
     */
    void register(RegisterRequest request);

    /**
     * 用户登录。
     *
     * @param request 登录参数
     * @return 登录结果
     */
    LoginResponse login(LoginRequest request);



}
