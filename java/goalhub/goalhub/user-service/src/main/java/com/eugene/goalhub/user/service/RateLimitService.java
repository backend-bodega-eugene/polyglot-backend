package com.eugene.goalhub.user.service;

public interface RateLimitService {

    void checkRegisterIpLimit(String ip);

    void checkLoginIpLimit(String ip);

    void checkLoginAccountLocked(String account);

    void recordLoginFail(String account);

    void clearLoginFail(String account);
}