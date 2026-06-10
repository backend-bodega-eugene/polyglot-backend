package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.user.entity.AccountTransactionEntity;
import com.eugene.goalhub.user.entity.UserAccountEntity;
import com.eugene.goalhub.user.mapper.AccountTransactionMapper;
import com.eugene.goalhub.user.mapper.UserAccountMapper;
import com.eugene.goalhub.user.service.InternalOrderAccountService;
import dto.DeductDefaultAccountRequest;
import dto.DeductDefaultAccountResponse;
import dto.DefaultAccountBalanceChangeRequest;
import exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 订单内部账户服务实现。
 *
 * <p>负责 order-service 下单、充值、提现时操作用户默认 USDT 账户，并记录账户流水。</p>
 */
@Service
public class InternalOrderAccountServiceImpl
        implements InternalOrderAccountService {

    private static final String MODULE_NAME = "订单内部账户";

    private static final String DEFAULT_CURRENCY_CODE = "USDT";

    private static final int MONEY_SCALE = 4;

    private static final String BIZ_TYPE_BET_ORDER = "BET_ORDER";

    private static final String BIZ_TYPE_DEPOSIT_SUCCESS = "DEPOSIT_SUCCESS";

    private static final String BIZ_TYPE_WITHDRAW_FREEZE = "WITHDRAW_FREEZE";

    private static final String BIZ_TYPE_WITHDRAW_SUCCESS = "WITHDRAW_SUCCESS";

    private static final String BIZ_TYPE_WITHDRAW_UNFREEZE = "WITHDRAW_UNFREEZE";

    private final UserAccountMapper userAccountMapper;

    private final AccountTransactionMapper accountTransactionMapper;

    private final GoalhubLogService goalhubLogService;

    public InternalOrderAccountServiceImpl(
            UserAccountMapper userAccountMapper,
            AccountTransactionMapper accountTransactionMapper,
            GoalhubLogService goalhubLogService) {
        this.userAccountMapper = userAccountMapper;
        this.accountTransactionMapper = accountTransactionMapper;
        this.goalhubLogService = goalhubLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeductDefaultAccountResponse deductDefaultUsdt(
            DeductDefaultAccountRequest request) {

        checkDeductRequest(request);

        UserAccountEntity account =
                getDefaultUsdtAccountForUpdate(request.getUserId());

        AccountTransactionEntity exists =
                findTransaction(BIZ_TYPE_BET_ORDER, request.getBizId());

        if (exists != null) {
            DeductDefaultAccountResponse response =
                    new DeductDefaultAccountResponse();

            response.setAccountId(account.getId());
            response.setCurrencyCode(account.getCurrencyCode());
            response.setBalanceBefore(safeAmount(exists.getBeforeBalance()));
            response.setBalanceAfter(safeAmount(exists.getAfterBalance()));

            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "DEDUCT_DEFAULT_USDT_IDEMPOTENT",
                    "订单扣减默认 USDT 账户幂等返回，accountId=" + account.getId()
                            + ", bizId=" + request.getBizId()
            );
            return response;
        }

        BigDecimal amount = normalizeMoney(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal beforeBalance = safeAmount(account.getBalance());
        BigDecimal afterBalance = normalizeMoney(beforeBalance.subtract(amount));

        if (afterBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.BALANCE_NOT_ENOUGH);
        }

        account.setBalance(afterBalance);

        updateAccountOrThrow(account);

        insertTransactionWithRollback(
                account,
                BIZ_TYPE_BET_ORDER,
                request.getBizId(),
                amount.negate(),
                beforeBalance,
                afterBalance,
                request.getRemark()
        );

        DeductDefaultAccountResponse response =
                new DeductDefaultAccountResponse();

        response.setAccountId(account.getId());
        response.setCurrencyCode(account.getCurrencyCode());
        response.setBalanceBefore(beforeBalance);
        response.setBalanceAfter(afterBalance);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "DEDUCT_DEFAULT_USDT",
                account.getUserId(),
                null,
                "订单扣减默认 USDT 账户成功，accountId=" + account.getId()
                        + ", bizId=" + request.getBizId()
                        + ", amount=" + amount
                        + ", beforeBalance=" + beforeBalance
                        + ", afterBalance=" + afterBalance
        );

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDefaultUsdt(
            DefaultAccountBalanceChangeRequest request) {

        checkDefaultChangeRequest(request);

        UserAccountEntity account =
                getDefaultUsdtAccountForUpdate(request.getUserId());

        if (findTransaction(BIZ_TYPE_DEPOSIT_SUCCESS, request.getBizId()) != null) {
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "ADD_DEFAULT_USDT_IDEMPOTENT",
                    "充值加款默认 USDT 账户幂等返回，accountId=" + account.getId()
                            + ", bizId=" + request.getBizId()
            );
            return;
        }

        BigDecimal amount = normalizeMoney(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal beforeBalance = safeAmount(account.getBalance());
        BigDecimal afterBalance = normalizeMoney(beforeBalance.add(amount));

        account.setBalance(afterBalance);

        updateAccountOrThrow(account);

        insertTransactionWithRollback(
                account,
                BIZ_TYPE_DEPOSIT_SUCCESS,
                request.getBizId(),
                amount,
                beforeBalance,
                afterBalance,
                request.getRemark()
        );

        goalhubLogService.bizLog(
                MODULE_NAME,
                "ADD_DEFAULT_USDT",
                account.getUserId(),
                null,
                "充值加款默认 USDT 账户成功，accountId=" + account.getId()
                        + ", bizId=" + request.getBizId()
                        + ", amount=" + amount
                        + ", beforeBalance=" + beforeBalance
                        + ", afterBalance=" + afterBalance
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeDefaultUsdt(
            DefaultAccountBalanceChangeRequest request) {

        checkDefaultChangeRequest(request);

        UserAccountEntity account =
                getDefaultUsdtAccountForUpdate(request.getUserId());

        if (findTransaction(BIZ_TYPE_WITHDRAW_FREEZE, request.getBizId()) != null) {
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "FREEZE_DEFAULT_USDT_IDEMPOTENT",
                    "提现冻结默认 USDT 账户幂等返回，accountId=" + account.getId()
                            + ", bizId=" + request.getBizId()
            );
            return;
        }

        BigDecimal amount = normalizeMoney(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal balance = safeAmount(account.getBalance());
        BigDecimal frozenBalance = safeAmount(account.getFrozenBalance());
        BigDecimal availableBefore = normalizeMoney(balance.subtract(frozenBalance));

        if (availableBefore.compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.BALANCE_NOT_ENOUGH);
        }

        BigDecimal frozenAfter = normalizeMoney(frozenBalance.add(amount));
        BigDecimal availableAfter = normalizeMoney(balance.subtract(frozenAfter));

        account.setFrozenBalance(frozenAfter);

        updateAccountOrThrow(account);

        insertTransactionWithRollback(
                account,
                BIZ_TYPE_WITHDRAW_FREEZE,
                request.getBizId(),
                amount.negate(),
                availableBefore,
                availableAfter,
                request.getRemark()
        );

        goalhubLogService.bizLog(
                MODULE_NAME,
                "FREEZE_DEFAULT_USDT",
                account.getUserId(),
                null,
                "提现冻结默认 USDT 账户成功，accountId=" + account.getId()
                        + ", bizId=" + request.getBizId()
                        + ", amount=" + amount
                        + ", availableBefore=" + availableBefore
                        + ", availableAfter=" + availableAfter
                        + ", frozenAfter=" + frozenAfter
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmFrozenDefaultUsdt(
            DefaultAccountBalanceChangeRequest request) {

        checkDefaultChangeRequest(request);

        UserAccountEntity account =
                getDefaultUsdtAccountForUpdate(request.getUserId());

        if (findTransaction(BIZ_TYPE_WITHDRAW_SUCCESS, request.getBizId()) != null) {
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "CONFIRM_FROZEN_DEFAULT_USDT_IDEMPOTENT",
                    "提现确认扣减默认 USDT 冻结余额幂等返回，accountId=" + account.getId()
                            + ", bizId=" + request.getBizId()
            );
            return;
        }

        BigDecimal amount = normalizeMoney(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal beforeBalance = safeAmount(account.getBalance());
        BigDecimal beforeFrozenBalance = safeAmount(account.getFrozenBalance());

        if (beforeFrozenBalance.compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.BALANCE_NOT_ENOUGH);
        }

        BigDecimal afterBalance = normalizeMoney(beforeBalance.subtract(amount));
        BigDecimal afterFrozenBalance = normalizeMoney(beforeFrozenBalance.subtract(amount));

        if (afterBalance.compareTo(BigDecimal.ZERO) < 0
                || afterFrozenBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.BALANCE_NOT_ENOUGH);
        }

        account.setBalance(afterBalance);
        account.setFrozenBalance(afterFrozenBalance);

        updateAccountOrThrow(account);

        insertTransactionWithRollback(
                account,
                BIZ_TYPE_WITHDRAW_SUCCESS,
                request.getBizId(),
                amount.negate(),
                beforeBalance,
                afterBalance,
                request.getRemark()
        );

        goalhubLogService.bizLog(
                MODULE_NAME,
                "CONFIRM_FROZEN_DEFAULT_USDT",
                account.getUserId(),
                null,
                "提现审核通过扣减冻结余额成功，accountId=" + account.getId()
                        + ", bizId=" + request.getBizId()
                        + ", amount=" + amount
                        + ", beforeBalance=" + beforeBalance
                        + ", afterBalance=" + afterBalance
                        + ", beforeFrozenBalance=" + beforeFrozenBalance
                        + ", afterFrozenBalance=" + afterFrozenBalance
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeDefaultUsdt(
            DefaultAccountBalanceChangeRequest request) {

        checkDefaultChangeRequest(request);

        UserAccountEntity account =
                getDefaultUsdtAccountForUpdate(request.getUserId());

        if (findTransaction(BIZ_TYPE_WITHDRAW_UNFREEZE, request.getBizId()) != null) {
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "UNFREEZE_DEFAULT_USDT_IDEMPOTENT",
                    "提现拒绝解冻默认 USDT 账户幂等返回，accountId=" + account.getId()
                            + ", bizId=" + request.getBizId()
            );
            return;
        }

        BigDecimal amount = normalizeMoney(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal balance = safeAmount(account.getBalance());
        BigDecimal beforeFrozenBalance = safeAmount(account.getFrozenBalance());

        if (beforeFrozenBalance.compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.BALANCE_NOT_ENOUGH);
        }

        BigDecimal availableBefore = normalizeMoney(balance.subtract(beforeFrozenBalance));
        BigDecimal afterFrozenBalance = normalizeMoney(beforeFrozenBalance.subtract(amount));
        BigDecimal availableAfter = normalizeMoney(balance.subtract(afterFrozenBalance));

        account.setFrozenBalance(afterFrozenBalance);

        updateAccountOrThrow(account);

        insertTransactionWithRollback(
                account,
                BIZ_TYPE_WITHDRAW_UNFREEZE,
                request.getBizId(),
                amount,
                availableBefore,
                availableAfter,
                request.getRemark()
        );

        goalhubLogService.bizLog(
                MODULE_NAME,
                "UNFREEZE_DEFAULT_USDT",
                account.getUserId(),
                null,
                "提现拒绝解冻默认 USDT 账户成功，accountId=" + account.getId()
                        + ", bizId=" + request.getBizId()
                        + ", amount=" + amount
                        + ", availableBefore=" + availableBefore
                        + ", availableAfter=" + availableAfter
                        + ", afterFrozenBalance=" + afterFrozenBalance
        );
    }

    private UserAccountEntity getDefaultUsdtAccountForUpdate(
            Long userId) {

        UserAccountEntity account =
                userAccountMapper.selectByUserIdAndCurrencyForUpdate(
                        userId,
                        DEFAULT_CURRENCY_CODE
                );

        if (account == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        if (!Integer.valueOf(1).equals(account.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        return account;
    }

    private AccountTransactionEntity findTransaction(
            String bizType,
            String bizId) {

        return accountTransactionMapper.selectOne(
                Wrappers.lambdaQuery(AccountTransactionEntity.class)
                        .eq(AccountTransactionEntity::getBizType, bizType)
                        .eq(AccountTransactionEntity::getBizId, bizId)
                        .last("LIMIT 1")
        );
    }

    private void insertTransactionWithRollback(
            UserAccountEntity account,
            String bizType,
            String bizId,
            BigDecimal changeAmount,
            BigDecimal beforeBalance,
            BigDecimal afterBalance,
            String remark) {

        AccountTransactionEntity transaction =
                new AccountTransactionEntity();

        transaction.setUserId(account.getUserId());
        transaction.setAccountId(account.getId());
        transaction.setCurrencyCode(account.getCurrencyCode());
        transaction.setBizType(bizType);
        transaction.setBizId(bizId);
        transaction.setChangeAmount(normalizeMoney(changeAmount));
        transaction.setBeforeBalance(safeAmount(beforeBalance));
        transaction.setAfterBalance(safeAmount(afterBalance));
        transaction.setRemark(remark);

        try {
            accountTransactionMapper.insert(transaction);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void updateAccountOrThrow(
            UserAccountEntity account) {

        int affectedRows = userAccountMapper.updateById(account);

        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void checkDeductRequest(
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

    private void checkDefaultChangeRequest(
            DefaultAccountBalanceChangeRequest request) {

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
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN);
        }

        return normalizeMoney(amount);
    }

    private BigDecimal normalizeMoney(
            BigDecimal amount) {

        return amount.setScale(MONEY_SCALE, RoundingMode.DOWN);
    }
}
