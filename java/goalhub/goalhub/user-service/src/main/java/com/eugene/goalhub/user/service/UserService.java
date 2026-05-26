package com.eugene.goalhub.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eugene.goalhub.user.entity.UserEntity;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;

public interface UserService {

    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);



}