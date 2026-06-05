package com.eugene.goalhub.user.service.impl;

import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.user.service.RateLimitService;
import exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.concurrent.TimeUnit;

/**
 * Redis 限流服务实现。
 *
 * <p>负责注册 IP 限流、登录 IP 限流、登录失败次数统计和账号短期锁定。</p>
 */
@Service
public class RateLimitServiceImpl implements RateLimitService {

    /**
     * 系统日志模块名称。
     */
    private static final String MODULE_NAME = "用户限流";

    /**
     * 注册 IP 限流 Redis key 前缀。
     */
    private static final String REGISTER_IP_KEY_PREFIX = "rate:register:ip:";

    /**
     * 登录 IP 限流 Redis key 前缀。
     */
    private static final String LOGIN_IP_KEY_PREFIX = "rate:login:ip:";

    /**
     * 登录失败账号计数 Redis key 前缀。
     */
    private static final String LOGIN_FAIL_ACCOUNT_KEY_PREFIX = "login:fail:account:";

    /**
     * 登录锁定账号 Redis key 前缀。
     */
    private static final String LOGIN_LOCK_ACCOUNT_KEY_PREFIX = "login:lock:account:";

    /**
     * Redis 字符串操作模板。
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建 Redis 限流服务实现。
     *
     * @param stringRedisTemplate Redis 字符串操作模板
     * @param goalhubLogService   日志写入服务
     */
    public RateLimitServiceImpl(StringRedisTemplate stringRedisTemplate,
                                GoalhubLogService goalhubLogService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 校验注册 IP 是否超过频率限制。
     *
     * @param ip 客户端 IP
     */
    @Override
    public void checkRegisterIpLimit(String ip) {
        validateNotBlank(ip);
        String key = REGISTER_IP_KEY_PREFIX + ip;
        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }

        if (count != null && count > 3) {
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "REGISTER_IP_LIMITED",
                    "注册 IP 触发限流，ip=" + ip + ", count=" + count
            );
            throw new BusinessException(ResultCode.REGISTER_TOO_FREQUENT);
        }
    }

    /**
     * 校验登录 IP 是否超过频率限制。
     *
     * @param ip 客户端 IP
     */
    @Override
    public void checkLoginIpLimit(String ip) {
        validateNotBlank(ip);
        String key = LOGIN_IP_KEY_PREFIX + ip;
        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }

        if (count != null && count > 20) {
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "LOGIN_IP_LIMITED",
                    "登录 IP 触发限流，ip=" + ip + ", count=" + count
            );
            throw new BusinessException(ResultCode.LOGIN_TOO_FREQUENT);
        }
    }

    /**
     * 校验登录账号是否处于锁定状态。
     *
     * @param account 登录账号
     */
    @Override
    public void checkLoginAccountLocked(String account) {
        validateNotBlank(account);
        String key = LOGIN_LOCK_ACCOUNT_KEY_PREFIX + account;
        Boolean locked = stringRedisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(locked)) {
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "LOGIN_ACCOUNT_LOCKED",
                    "登录账号处于锁定状态，account=" + account
            );
            throw new BusinessException(ResultCode.LOGIN_ACCOUNT_LOCKED);
        }
    }

    /**
     * 记录登录失败次数，并在达到阈值时锁定账号。
     *
     * @param account 登录账号
     */
    @Override
    public void recordLoginFail(String account) {
        validateNotBlank(account);
        String failKey = LOGIN_FAIL_ACCOUNT_KEY_PREFIX + account;
        Long count = stringRedisTemplate.opsForValue().increment(failKey);

        if (count != null && count == 1) {
            stringRedisTemplate.expire(failKey, 1, TimeUnit.MINUTES);
        }

        if (count != null && count >= 3) {
            String lockKey = LOGIN_LOCK_ACCOUNT_KEY_PREFIX + account;
            stringRedisTemplate.opsForValue().set(lockKey, "1", 10, TimeUnit.MINUTES);
            stringRedisTemplate.delete(failKey);
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "LOCK_LOGIN_ACCOUNT",
                    "登录失败次数达到阈值，锁定账号，account=" + account + ", count=" + count
            );
            throw new BusinessException(ResultCode.LOGIN_ACCOUNT_LOCKED);
        }
    }

    /**
     * 清理登录失败次数和账号锁定状态。
     *
     * @param account 登录账号
     */
    @Override
    public void clearLoginFail(String account) {
        validateNotBlank(account);
        stringRedisTemplate.delete(LOGIN_FAIL_ACCOUNT_KEY_PREFIX + account);
        stringRedisTemplate.delete(LOGIN_LOCK_ACCOUNT_KEY_PREFIX + account);
    }

    /**
     * 校验限流 key 片段不能为空。
     *
     * @param value key 片段
     */
    private void validateNotBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
