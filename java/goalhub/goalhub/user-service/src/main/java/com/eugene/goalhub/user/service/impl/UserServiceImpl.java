package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.user.entity.UserAccountEntity;
import com.eugene.goalhub.user.entity.UserEntity;
import com.eugene.goalhub.user.mapper.UserAccountMapper;
import com.eugene.goalhub.user.mapper.UserMapper;
import com.eugene.goalhub.user.service.CaptchaService;
import com.eugene.goalhub.user.service.RateLimitService;
import com.eugene.goalhub.user.service.UserService;
import dto.*;
import exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;
import utils.JwtUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements UserService {

    private static final String MODULE_NAME = "用户账号";

    private final PasswordEncoder passwordEncoder;
    private final UserAccountMapper userAccountMapper;
    private final CaptchaService captchaService;
    private final RateLimitService rateLimitService;
    private final GoalhubLogService goalhubLogService;

    public UserServiceImpl(PasswordEncoder passwordEncoder,
                           UserAccountMapper userAccountMapper,
                           CaptchaService captchaService,
                           RateLimitService rateLimitService,
                           GoalhubLogService goalhubLogService) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountMapper = userAccountMapper;
        this.captchaService = captchaService;
        this.rateLimitService = rateLimitService;
        this.goalhubLogService = goalhubLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request, String clientIp) {

        rateLimitService.checkRegisterIpLimit(clientIp);

        captchaService.validate(
                request.getCaptchaKey(),
                request.getCaptchaCode()
        );

        String username = trimToNull(request.getUsername());
        String password = trimToNull(request.getPassword());
        String email = trimToNull(request.getEmail());
        String phone = trimToNull(request.getPhone());

        if (username == null) {
            throw new BusinessException(ResultCode.USERNAME_NOT_NULL);
        }

        if (password == null) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_NULL);
        }

        Long usernameCount = lambdaQuery()
                .eq(UserEntity::getUsername, username)
                .count();

        if (usernameCount > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        if (email != null) {
            Long emailCount = lambdaQuery()
                    .eq(UserEntity::getEmail, email)
                    .count();

            if (emailCount > 0) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }

        if (phone != null) {
            Long phoneCount = lambdaQuery()
                    .eq(UserEntity::getPhone, phone)
                    .count();

            if (phoneCount > 0) {
                throw new BusinessException(ResultCode.PHONE_EXISTS);
            }
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
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

        goalhubLogService.bizLog(
                MODULE_NAME,
                "USER_REGISTER",
                user.getId(),
                user.getUsername(),
                "用户注册成功，userId=" + user.getId()
                        + ", username=" + user.getUsername()
                        + ", clientIp=" + clientIp
        );
    }

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {

        rateLimitService.checkLoginIpLimit(clientIp);

        if (request.getAccount() == null) {
            throw new BusinessException(ResultCode.USERNAME_NOT_NULL);
        }

        String account = request.getAccount().trim();

        if (account.isEmpty()) {
            throw new BusinessException(ResultCode.USERNAME_NOT_NULL);
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PASSWORD_NOT_NULL);
        }

        rateLimitService.checkLoginAccountLocked(account);

        captchaService.validate(
                request.getCaptchaKey(),
                request.getCaptchaCode()
        );

        UserEntity user = lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(UserEntity::getUsername, account)
                        .or()
                        .eq(UserEntity::getEmail, account)
                        .or()
                        .eq(UserEntity::getPhone, account))
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

        goalhubLogService.bizLog(
                MODULE_NAME,
                "USER_LOGIN",
                user.getId(),
                user.getUsername(),
                "用户登录成功，userId=" + user.getId()
                        + ", username=" + user.getUsername()
                        + ", clientIp=" + clientIp
        );
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserEntity user = getById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        boolean matched = passwordEncoder.matches(
                request.getOldPassword(),
                user.getPasswordHash()
        );

        if (!matched) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        updateById(user);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "CHANGE_PASSWORD",
                user.getId(),
                user.getUsername(),
                "用户修改密码成功，userId=" + user.getId()
        );
    }

    @Override
    public UserProfileResponse getProfile(Long userId) {
        UserEntity user = getById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return toProfileResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserProfileUpdateRequest request) {
        UserEntity user = getById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String email = trimToNull(request.getEmail());
        String phone = trimToNull(request.getPhone());

        if (email != null) {
            Long count = lambdaQuery()
                    .eq(UserEntity::getEmail, email)
                    .ne(UserEntity::getId, userId)
                    .count();

            if (count > 0) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }

            user.setEmail(email);
        }

        if (phone != null) {
            Long count = lambdaQuery()
                    .eq(UserEntity::getPhone, phone)
                    .ne(UserEntity::getId, userId)
                    .count();

            if (count > 0) {
                throw new BusinessException(ResultCode.PHONE_EXISTS);
            }

            user.setPhone(phone);
        }

        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());

        updateById(user);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "UPDATE_PROFILE",
                user.getId(),
                user.getUsername(),
                "用户修改资料成功，userId=" + user.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setFundPassword(Long userId, SetFundPasswordRequest request) {
        UserEntity user = getById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (user.getFundPasswordHash() != null && !user.getFundPasswordHash().isBlank()) {
            throw new BusinessException(ResultCode.FUND_PASSWORD_ALREADY_SET);
        }

        String fundPassword = trimToNull(request.getFundPassword());

        if (fundPassword == null) {
            throw new BusinessException(ResultCode.FUND_PASSWORD_NOT_NULL);
        }

        user.setFundPasswordHash(passwordEncoder.encode(fundPassword));
        updateById(user);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "SET_FUND_PASSWORD",
                user.getId(),
                user.getUsername(),
                "用户设置资金密码成功，userId=" + user.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeFundPassword(Long userId, ChangeFundPasswordRequest request) {
        UserEntity user = getById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (user.getFundPasswordHash() == null || user.getFundPasswordHash().isBlank()) {
            throw new BusinessException(ResultCode.FUND_PASSWORD_NOT_SET);
        }

        String oldFundPassword = trimToNull(request.getOldFundPassword());
        String newFundPassword = trimToNull(request.getNewFundPassword());

        if (oldFundPassword == null || newFundPassword == null) {
            throw new BusinessException(ResultCode.FUND_PASSWORD_NOT_NULL);
        }

        boolean matched = passwordEncoder.matches(
                oldFundPassword,
                user.getFundPasswordHash()
        );

        if (!matched) {
            throw new BusinessException(ResultCode.FUND_PASSWORD_ERROR);
        }

        user.setFundPasswordHash(passwordEncoder.encode(newFundPassword));
        updateById(user);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "CHANGE_FUND_PASSWORD",
                user.getId(),
                user.getUsername(),
                "用户修改资金密码成功，userId=" + user.getId()
        );
    }

    @Override
    public void verifyFundPassword(Long userId, String fundPassword) {
        if (userId == null) {
            throw new BusinessException(ResultCode.USER_ID_NOT_NULL);
        }

        UserEntity user = getById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (user.getFundPasswordHash() == null || user.getFundPasswordHash().isBlank()) {
            throw new BusinessException(ResultCode.FUND_PASSWORD_NOT_SET);
        }

        if (fundPassword == null || fundPassword.trim().isEmpty()) {
            throw new BusinessException(ResultCode.FUND_PASSWORD_NOT_NULL);
        }

        boolean matched = passwordEncoder.matches(
                fundPassword,
                user.getFundPasswordHash()
        );

        if (!matched) {
            throw new BusinessException(ResultCode.FUND_PASSWORD_ERROR);
        }
    }

    private UserProfileResponse toProfileResponse(UserEntity user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setStatus(user.getStatus());
        response.setHasFundPassword(
                user.getFundPasswordHash() != null && !user.getFundPasswordHash().isBlank()
        );
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }
}