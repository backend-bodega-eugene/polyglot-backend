package com.eugene.goalhub.user.service.impl;

import com.eugene.goalhub.user.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import dto.CaptchaResponse;
import exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    private final StringRedisTemplate stringRedisTemplate;

    public CaptchaServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public CaptchaResponse generate() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(SpecCaptcha.TYPE_ONLY_NUMBER);

        String captchaKey = UUID.randomUUID().toString();
        String captchaCode = captcha.text();

        stringRedisTemplate.opsForValue().set(
                CAPTCHA_KEY_PREFIX + captchaKey,
                captchaCode,
                5,
                TimeUnit.MINUTES
        );

        CaptchaResponse response = new CaptchaResponse();
        response.setCaptchaKey(captchaKey);
        response.setCaptchaImage(
                captcha.toBase64().replace("data:image/png;base64,", "")
        );

        return response;
    }

    @Override
    public void validate(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaKey.trim().isEmpty()
                || captchaCode == null || captchaCode.trim().isEmpty()) {
            throw new BusinessException(ResultCode.CAPTCHA_NOT_NULL);
        }

        String redisKey = CAPTCHA_KEY_PREFIX + captchaKey.trim();
        String realCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (realCode == null) {
            throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
        }

        stringRedisTemplate.delete(redisKey);

        if (!realCode.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
    }
}