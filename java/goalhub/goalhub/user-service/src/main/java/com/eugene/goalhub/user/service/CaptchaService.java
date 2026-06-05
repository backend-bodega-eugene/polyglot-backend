package com.eugene.goalhub.user.service;

import dto.CaptchaResponse;

/**
 * 图形验证码服务。
 *
 * <p>定义验证码生成和校验能力。</p>
 */
public interface CaptchaService {

    /**
     * 生成图形验证码。
     *
     * @return 图形验证码响应
     */
    CaptchaResponse generate();

    /**
     * 校验图形验证码。
     *
     * @param captchaKey  验证码标识
     * @param captchaCode 用户输入的验证码
     */
    void validate(String captchaKey, String captchaCode);
}
