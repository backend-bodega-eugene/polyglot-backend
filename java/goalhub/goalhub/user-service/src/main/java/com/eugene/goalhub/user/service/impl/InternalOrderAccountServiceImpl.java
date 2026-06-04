package com.eugene.goalhub.user.service.impl;

import com.eugene.goalhub.user.entity.AccountTransactionEntity;
import com.eugene.goalhub.user.entity.UserAccountEntity;
import com.eugene.goalhub.user.mapper.AccountTransactionMapper;
import com.eugene.goalhub.user.mapper.UserAccountMapper;
import com.eugene.goalhub.user.service.InternalOrderAccountService;
import dto.DeductDefaultAccountRequest;
import dto.DeductDefaultAccountResponse;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;

/**
 * 订单内部账户服务实现。
 */
@Service
public class InternalOrderAccountServiceImpl
        implements InternalOrderAccountService {

    private static final String DEFAULT_CURRENCY_CODE = "USDT";

    private static final String BIZ_TYPE_BET_ORDER = "BET_ORDER";

    private final UserAccountMapper userAccountMapper;

    private final AccountTransactionMapper accountTransactionMapper;

    public InternalOrderAccountServiceImpl(
            UserAccountMapper userAccountMapper,
            AccountTransactionMapper accountTransactionMapper) {
        this.userAccountMapper = userAccountMapper;
        this.accountTransactionMapper = accountTransactionMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeductDefaultAccountResponse deductDefaultUsdt(
            DeductDefaultAccountRequest request) {

        checkRequest(request);

        UserAccountEntity account =
                userAccountMapper.selectByUserIdAndCurrencyForUpdate(
                        request.getUserId(),
                        DEFAULT_CURRENCY_CODE
                );

        if (account == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        if (!Integer.valueOf(1).equals(account.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        BigDecimal beforeBalance = safeAmount(account.getBalance());
        BigDecimal afterBalance = beforeBalance.subtract(request.getAmount());

        if (afterBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.BALANCE_NOT_ENOUGH);
        }

        account.setBalance(afterBalance);

        userAccountMapper.updateById(account);

        AccountTransactionEntity transaction =
                new AccountTransactionEntity();

        transaction.setUserId(account.getUserId());
        transaction.setAccountId(account.getId());
        transaction.setCurrencyCode(account.getCurrencyCode());
        transaction.setBizType(BIZ_TYPE_BET_ORDER);
        transaction.setBizId(request.getBizId());
        transaction.setChangeAmount(request.getAmount().negate());
        transaction.setBeforeBalance(beforeBalance);
        transaction.setAfterBalance(afterBalance);
        transaction.setRemark(request.getRemark());

        accountTransactionMapper.insert(transaction);

        DeductDefaultAccountResponse response =
                new DeductDefaultAccountResponse();

        response.setAccountId(account.getId());
        response.setCurrencyCode(account.getCurrencyCode());
        response.setBalanceBefore(beforeBalance);
        response.setBalanceAfter(afterBalance);

        return response;
    }

    private void checkRequest(
            DeductDefaultAccountRequest request) {

        if (request == null
                || request.getUserId() == null
                || request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0
                || request.getBizId() == null
                || request.getBizId().isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private BigDecimal safeAmount(
            BigDecimal amount) {

        if (amount == null) {
            return BigDecimal.ZERO;
        }

        return amount;
    }
}