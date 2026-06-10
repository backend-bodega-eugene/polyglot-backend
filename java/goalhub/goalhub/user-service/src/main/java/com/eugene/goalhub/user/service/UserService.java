package com.eugene.goalhub.user.service;

import dto.*;

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

    /**
     * 修改密码。
     *
     * @param userId  用户 ID
     * @param request 修改密码参数
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    UserProfileResponse getProfile(Long userId);

    void updateProfile(Long userId, UserProfileUpdateRequest request);

    void setFundPassword(Long userId, SetFundPasswordRequest request);

    void changeFundPassword(Long userId, ChangeFundPasswordRequest request);
    void verifyFundPassword(Long userId, String fundPassword);
}
