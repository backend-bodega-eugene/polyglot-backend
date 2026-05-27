package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.user.entity.UserEntity;
import com.eugene.goalhub.user.mapper.UserMapper;
import com.eugene.goalhub.user.service.UserService;
import utils.JwtUtil;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;
import exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import response.ResultCode;

@Service
public class UserServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements UserService {

    private final PasswordEncoder passwordEncoder;
    //private final JwtUtil jwtUtil;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
       // this.jwtUtil = jwtUtil;
    }
    @Override
    public void register(RegisterRequest request) {

        String username = request.getUsername();

        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ResultCode.USERNAME_NOT_NULL);
        }

        username = username.trim();

        Long count = lambdaQuery()
                .eq(UserEntity::getUsername, username)
                .count();

        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        UserEntity user = new UserEntity();

        user.setUsername(username);

        // 邮箱
        if (username.contains("@")) {
            user.setEmail(username);
        }

        // 手机号
        if (username.matches("^\\+?\\d+$")) {
            user.setPhone(username);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus(1);

        save(user);
    }
    @Override
    public LoginResponse login(LoginRequest request) {

        String account = request.getAccount();

        if (account == null || account.trim().isEmpty()) {
            throw new BusinessException(ResultCode.USERNAME_NOT_NULL);
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_NULL);
        }

        account = account.trim();

        UserEntity user = lambdaQuery()
                .eq(UserEntity::getUsername, account)
                .or()
                .eq(UserEntity::getEmail, account)
                .or()
                .eq(UserEntity::getPhone, account)
                .one();

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        boolean matched = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!matched) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String token = JwtUtil.userGenerateToken(user.getId(), user.getUsername());

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setToken(token);

        return response;
    }
}