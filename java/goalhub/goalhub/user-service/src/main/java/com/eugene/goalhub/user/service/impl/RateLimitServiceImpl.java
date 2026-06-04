package com.eugene.goalhub.user.service.impl;

import com.eugene.goalhub.user.service.RateLimitService;
import exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private static final String REGISTER_IP_KEY_PREFIX = "rate:register:ip:";
    private static final String LOGIN_IP_KEY_PREFIX = "rate:login:ip:";
    private static final String LOGIN_FAIL_ACCOUNT_KEY_PREFIX = "login:fail:account:";
    private static final String LOGIN_LOCK_ACCOUNT_KEY_PREFIX = "login:lock:account:";

    private final StringRedisTemplate stringRedisTemplate;

    public RateLimitServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void checkRegisterIpLimit(String ip) {
        String key = REGISTER_IP_KEY_PREFIX + ip;
        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }

        if (count != null && count > 3) {
            throw new BusinessException(ResultCode.REGISTER_TOO_FREQUENT);
        }
    }

    @Override
    public void checkLoginIpLimit(String ip) {
        String key = LOGIN_IP_KEY_PREFIX + ip;
        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }

        if (count != null && count > 20) {
            throw new BusinessException(ResultCode.LOGIN_TOO_FREQUENT);
        }
    }

    @Override
    public void checkLoginAccountLocked(String account) {
        String key = LOGIN_LOCK_ACCOUNT_KEY_PREFIX + account;
        Boolean locked = stringRedisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(locked)) {
            throw new BusinessException(ResultCode.LOGIN_ACCOUNT_LOCKED);
        }
    }

    @Override
    public void recordLoginFail(String account) {
        String failKey = LOGIN_FAIL_ACCOUNT_KEY_PREFIX + account;
        Long count = stringRedisTemplate.opsForValue().increment(failKey);

        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey, 1, TimeUnit.MINUTES);
        }

        if (count != null && count >= 3) {
            String lockKey = LOGIN_LOCK_ACCOUNT_KEY_PREFIX + account;
            stringRedisTemplate.opsForValue().set(lockKey, "1", 10, TimeUnit.MINUTES);
            stringRedisTemplate.delete(failKey);
            throw new BusinessException(ResultCode.LOGIN_ACCOUNT_LOCKED);
        }
    }

    @Override
    public void clearLoginFail(String account) {
        stringRedisTemplate.delete(LOGIN_FAIL_ACCOUNT_KEY_PREFIX + account);
        stringRedisTemplate.delete(LOGIN_LOCK_ACCOUNT_KEY_PREFIX + account);
    }
}