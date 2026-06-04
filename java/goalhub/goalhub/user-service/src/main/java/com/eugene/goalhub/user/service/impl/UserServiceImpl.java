package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.user.entity.UserAccountEntity;
import com.eugene.goalhub.user.entity.UserEntity;
import com.eugene.goalhub.user.mapper.UserAccountMapper;
import com.eugene.goalhub.user.mapper.UserMapper;
import com.eugene.goalhub.user.service.CaptchaService;
import com.eugene.goalhub.user.service.RateLimitService;
import com.eugene.goalhub.user.service.UserService;
import dto.LoginRequest;
import dto.LoginResponse;
import dto.RegisterRequest;
import exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;
import utils.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户账号服务实现。
 */
@Service
public class UserServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements UserService {

    private final PasswordEncoder passwordEncoder;

    private final UserAccountMapper userAccountMapper;

    private final CaptchaService captchaService;

    private final RateLimitService rateLimitService;

    public UserServiceImpl(PasswordEncoder passwordEncoder,
                           UserAccountMapper userAccountMapper,
                           CaptchaService captchaService,
                           RateLimitService rateLimitService) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountMapper = userAccountMapper;
        this.captchaService = captchaService;
        this.rateLimitService = rateLimitService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request, String clientIp) {

        rateLimitService.checkRegisterIpLimit(clientIp);

        captchaService.validate(
                request.getCaptchaKey(),
                request.getCaptchaCode()
        );

        String username = request.getUsername();

        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ResultCode.USERNAME_NOT_NULL);
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_NULL);
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

        if (username.contains("@")) {
            user.setEmail(username);
        }

        if (username.matches("^\\+?\\d+$")) {
            user.setPhone(username);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus(1);
        user.setRegisterIp(clientIp);

        save(user);

        UserAccountEntity account = new UserAccountEntity();
        account.setUserId(user.getId());
        account.setCurrencyCode("USDT");
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenBalance(BigDecimal.ZERO);
        account.setStatus(1);

        userAccountMapper.insert(account);
    }

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {

        rateLimitService.checkLoginIpLimit(clientIp);

        String account = request.getAccount();

        if (account == null || account.trim().isEmpty()) {
            throw new BusinessException(ResultCode.USERNAME_NOT_NULL);
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_NULL);
        }

        account = account.trim();

        rateLimitService.checkLoginAccountLocked(account);

        captchaService.validate(
                request.getCaptchaKey(),
                request.getCaptchaCode()
        );

        UserEntity user = lambdaQuery()
                .eq(UserEntity::getUsername, account)
                .or()
                .eq(UserEntity::getEmail, account)
                .or()
                .eq(UserEntity::getPhone, account)
                .one();

        if (user == null) {
            rateLimitService.recordLoginFail(account);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        boolean matched = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!matched) {
            rateLimitService.recordLoginFail(account);
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        rateLimitService.clearLoginFail(account);

        user.setLastLoginIp(clientIp);
        user.setLastLoginAt(LocalDateTime.now());
        updateById(user);

        String token = JwtUtil.userGenerateToken(user.getId(), user.getUsername());

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setToken(token);

        return response;
    }
}