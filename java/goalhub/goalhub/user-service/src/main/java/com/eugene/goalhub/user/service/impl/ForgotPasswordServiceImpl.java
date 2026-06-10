package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.user.entity.UserEntity;
import com.eugene.goalhub.user.mapper.UserMapper;
import com.eugene.goalhub.user.service.ForgotPasswordService;
import dto.ForgotPasswordResetRequest;
import dto.ForgotPasswordSendCodeRequest;
import exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class ForgotPasswordServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements ForgotPasswordService {

    private static final String MODULE_NAME = "找回密码";

    private static final String CODE_KEY_PREFIX = "goalhub:forgot-password:code:";

    private static final String LIMIT_KEY_PREFIX = "goalhub:forgot-password:limit:";

    private static final long CODE_TTL_MINUTES = 10L;

    private static final long SEND_LIMIT_SECONDS = 60L;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate stringRedisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final GoalhubLogService goalhubLogService;
    @Value("${spring.mail.username}")
    private String fromEmail;
    private final JavaMailSender javaMailSender;
    public ForgotPasswordServiceImpl(StringRedisTemplate stringRedisTemplate,
                                     PasswordEncoder passwordEncoder,
                                     GoalhubLogService goalhubLogService,
                                     JavaMailSender javaMailSender) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.goalhubLogService = goalhubLogService;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendCode(ForgotPasswordSendCodeRequest request) {

        String email = normalizeEmail(request.getEmail());

        String limitKey = buildLimitKey(email);
        Boolean limited = stringRedisTemplate.hasKey(limitKey);

        if (Boolean.TRUE.equals(limited)) {
            throw new BusinessException(ResultCode.EMAIL_CODE_SEND_TOO_FREQUENT);
        }

        UserEntity user = lambdaQuery()
                .eq(UserEntity::getEmail, email)
                .one();

        stringRedisTemplate.opsForValue().set(
                limitKey,
                "1",
                SEND_LIMIT_SECONDS,
                TimeUnit.SECONDS
        );

        if (user == null) {
            return;
        }

        String code = generateCode();

        stringRedisTemplate.opsForValue().set(
                buildCodeKey(email),
                code,
                CODE_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        sendEmailCode(email, code);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "SEND_FORGOT_PASSWORD_CODE",
                user.getId(),
                user.getUsername(),
                "发送找回密码邮箱验证码成功，userId=" + user.getId()
                        + ", email=" + email
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reset(ForgotPasswordResetRequest request) {

        String email = normalizeEmail(request.getEmail());

        UserEntity user = lambdaQuery()
                .eq(UserEntity::getEmail, email)
                .one();

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String codeKey = buildCodeKey(email);
        String redisCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (redisCode == null || redisCode.isBlank()) {
            throw new BusinessException(ResultCode.EMAIL_CODE_EXPIRED);
        }

        if (request.getCode() == null
                || !redisCode.equals(request.getCode().trim())) {
            throw new BusinessException(ResultCode.EMAIL_CODE_ERROR);
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword())
        );

        updateById(user);

        stringRedisTemplate.delete(codeKey);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "RESET_PASSWORD_BY_EMAIL",
                user.getId(),
                user.getUsername(),
                "用户通过邮箱验证码重置密码成功，userId=" + user.getId()
                        + ", email=" + email
        );
    }

    private String normalizeEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(ResultCode.EMAIL_NOT_NULL);
        }

        return email.trim().toLowerCase();
    }

    private String buildCodeKey(String email) {
        return CODE_KEY_PREFIX + email;
    }

    private String buildLimitKey(String email) {
        return LIMIT_KEY_PREFIX + email;
    }

    private String generateCode() {
        int number = RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private void sendEmailCode(String email, String code) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("GoalHub Password Reset Code");
        message.setText("Your password reset code is: " + code + "\n\nThis code is valid for 10 minutes.");

        javaMailSender.send(message);
    }
}