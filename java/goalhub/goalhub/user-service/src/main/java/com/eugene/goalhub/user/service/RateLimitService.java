package com.eugene.goalhub.user.service;

/**
 * 限流服务。
 *
 * <p>定义注册、登录和登录失败锁定相关的限流校验能力。</p>
 */
public interface RateLimitService {

    /**
     * 校验注册 IP 是否超过频率限制。
     *
     * @param ip 客户端 IP
     */
    void checkRegisterIpLimit(String ip);

    /**
     * 校验登录 IP 是否超过频率限制。
     *
     * @param ip 客户端 IP
     */
    void checkLoginIpLimit(String ip);

    /**
     * 校验登录账号是否处于锁定状态。
     *
     * @param account 登录账号
     */
    void checkLoginAccountLocked(String account);

    /**
     * 记录登录失败次数。
     *
     * @param account 登录账号
     */
    void recordLoginFail(String account);

    /**
     * 清理登录失败次数和锁定状态。
     *
     * @param account 登录账号
     */
    void clearLoginFail(String account);
}
