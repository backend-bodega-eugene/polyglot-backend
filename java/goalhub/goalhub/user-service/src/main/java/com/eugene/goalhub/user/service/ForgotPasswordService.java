package com.eugene.goalhub.user.service;

import dto.ForgotPasswordResetRequest;
import dto.ForgotPasswordSendCodeRequest;

public interface ForgotPasswordService {

    void sendCode(ForgotPasswordSendCodeRequest request);

    void reset(ForgotPasswordResetRequest request);
}