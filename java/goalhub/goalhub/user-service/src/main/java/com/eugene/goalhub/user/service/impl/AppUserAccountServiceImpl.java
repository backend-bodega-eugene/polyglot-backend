package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.user.entity.UserAccountEntity;
import com.eugene.goalhub.user.mapper.UserAccountMapper;
import com.eugene.goalhub.user.service.AppUserAccountService;
import dto.AppUserBalanceResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AppUserAccountServiceImpl implements AppUserAccountService {

    private static final String DEFAULT_CURRENCY_CODE = "USDT";

    private final UserAccountMapper userAccountMapper;

    public AppUserAccountServiceImpl(
            UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

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
            response.setBalance(BigDecimal.ZERO);
            response.setFrozenBalance(BigDecimal.ZERO);
            response.setAvailableBalance(BigDecimal.ZERO);
            response.setStatus(1);
            return response;
        }

        BigDecimal balance = safeAmount(account.getBalance());
        BigDecimal frozenBalance = safeAmount(account.getFrozenBalance());

        AppUserBalanceResponse response = new AppUserBalanceResponse();
        response.setAccountId(account.getId());
        response.setCurrencyCode(account.getCurrencyCode());
        response.setBalance(balance);
        response.setFrozenBalance(frozenBalance);
        response.setAvailableBalance(balance.subtract(frozenBalance));
        response.setStatus(account.getStatus());

        return response;
    }

    private BigDecimal safeAmount(
            BigDecimal amount) {

        if (amount == null) {
            return BigDecimal.ZERO;
        }

        return amount;
    }
}