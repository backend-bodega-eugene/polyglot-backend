package com.eugene.goalhub.user.service.impl;

import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.user.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import dto.CaptchaResponse;
import exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务实现。
 *
 * <p>负责生成数字图形验证码、写入 Redis，并在验证后删除验证码。</p>
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    /**
     * 系统日志模块名称。
     */
    private static final String MODULE_NAME = "图形验证码";

    /**
     * 图形验证码 Redis key 前缀。
     */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    /**
     * Redis 字符串操作模板。
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建图形验证码服务实现。
     *
     * @param stringRedisTemplate Redis 字符串操作模板
     * @param goalhubLogService   日志写入服务
     */
    public CaptchaServiceImpl(StringRedisTemplate stringRedisTemplate,
                              GoalhubLogService goalhubLogService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 生成图形验证码。
     *
     * @return 图形验证码响应
     */
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

        goalhubLogService.sysLog(
                MODULE_NAME,
                "GENERATE_CAPTCHA",
                "生成图形验证码成功，captchaKey=" + captchaKey
        );
        return response;
    }

    /**
     * 校验图形验证码。
     *
     * @param captchaKey  验证码标识
     * @param captchaCode 用户输入的验证码
     */
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
        goalhubLogService.sysLog(
                MODULE_NAME,
                "VALIDATE_CAPTCHA",
                "校验图形验证码成功，captchaKey=" + captchaKey.trim()
        );
    }
}
