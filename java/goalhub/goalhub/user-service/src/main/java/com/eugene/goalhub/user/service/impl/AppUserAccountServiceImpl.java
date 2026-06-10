package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.user.entity.UserAccountEntity;
import com.eugene.goalhub.user.mapper.UserAccountMapper;
import com.eugene.goalhub.user.service.AppUserAccountService;
import dto.AppUserBalanceResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 前端用户账户服务实现。
 *
 * <p>负责查询当前用户默认 USDT 账户余额，并组装前端余额响应。</p>
 */
@Service
public class AppUserAccountServiceImpl implements AppUserAccountService {

    /**
     * 系统日志模块名称。
     */
    private static final String MODULE_NAME = "前端用户账户";

    /**
     * 默认账户币种编码。
     */
    private static final String DEFAULT_CURRENCY_CODE = "USDT";

    /**
     * USDT 金额统一保留 4 位小数。
     */
    private static final int MONEY_SCALE = 4;

    /**
     * 用户账户 Mapper。
     */
    private final UserAccountMapper userAccountMapper;

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建前端用户账户服务实现。
     *
     * @param userAccountMapper 用户账户 Mapper
     * @param goalhubLogService 日志写入服务
     */
    public AppUserAccountServiceImpl(
            UserAccountMapper userAccountMapper,
            GoalhubLogService goalhubLogService) {
        this.userAccountMapper = userAccountMapper;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 查询当前用户默认 USDT 账户余额。
     *
     * @param userId 当前登录用户 ID
     * @return 默认账户余额响应
     */
    @Override
    public AppUserBalanceResponse getDefaultBalance(
            Long userId) {

        UserAccountEntity account = userAccountMapper.selectOne(
                Wrappers.lambdaQuery(UserAccountEntity.class)
                        .eq(UserAccountEntity::getUserId, userId)
                        .eq(UserAccountEntity::getCurrencyCode, DEFAULT_CURRENCY_CODE)
        );

        if (account == null) {
            AppUserBalanceResponse response = new AppUserBalanceResponse();
            response.setCurrencyCode(DEFAULT_CURRENCY_CODE);
            response.setBalance(zeroMoney());
            response.setFrozenBalance(zeroMoney());
            response.setAvailableBalance(zeroMoney());
            response.setStatus(1);
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "GET_DEFAULT_BALANCE",
                    "查询默认 USDT 账户余额，账户不存在返回默认余额，userId=" + userId
            );
            return response;
        }

        BigDecimal balance = safeAmount(account.getBalance());
        BigDecimal frozenBalance = safeAmount(account.getFrozenBalance());

        AppUserBalanceResponse response = new AppUserBalanceResponse();
        response.setAccountId(account.getId());
        response.setCurrencyCode(account.getCurrencyCode());
        response.setBalance(balance);
        response.setFrozenBalance(frozenBalance);
       // response.setAvailableBalance(balance.subtract(frozenBalance));
        //目前没有总资金概念,账户Balance字段就是可用金额,也许将来这里要改,回复扣减后才是可用金额
        response.setAvailableBalance(balance);
        response.setStatus(account.getStatus());

        goalhubLogService.sysLog(
                MODULE_NAME,
                "GET_DEFAULT_BALANCE",
                "查询默认 USDT 账户余额成功，userId=" + userId + ", accountId=" + account.getId()
        );
        return response;
    }

    /**
     * 获取安全金额，空值按 0 处理。
     *
     * @param amount 原始金额
     * @return 非空金额
     */
    private BigDecimal safeAmount(
            BigDecimal amount) {

        if (amount == null) {
            return zeroMoney();
        }

        return amount.setScale(MONEY_SCALE, RoundingMode.DOWN);
    }

    /**
     * 返回 4 位小数零金额。
     *
     * @return 0.0000
     */
    private BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN);
    }
}
