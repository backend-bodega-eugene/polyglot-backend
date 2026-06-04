package com.eugene.goalhub.user.service;

import dto.CaptchaResponse;

public interface CaptchaService {

    CaptchaResponse generate();

    void validate(String captchaKey, String captchaCode);
}