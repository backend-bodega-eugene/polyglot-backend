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
import exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;

/**
 * 订单内部账户服务实现。
 *
 * <p>负责 order-service 下单时扣减用户默认 USDT 账户余额，并记录账户流水。</p>
 */
@Service
public class InternalOrderAccountServiceImpl
        implements InternalOrderAccountService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "订单内部账户";

    /**
     * 默认扣款币种编码。
     */
    private static final String DEFAULT_CURRENCY_CODE = "USDT";

    /**
     * 投注订单业务类型。
     */
    private static final String BIZ_TYPE_BET_ORDER = "BET_ORDER";

    /**
     * 用户账户 Mapper。
     */
    private final UserAccountMapper userAccountMapper;

    /**
     * 账户流水 Mapper。
     */
    private final AccountTransactionMapper accountTransactionMapper;

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建订单内部账户服务实现。
     *
     * @param userAccountMapper        用户账户 Mapper
     * @param accountTransactionMapper 账户流水 Mapper
     * @param goalhubLogService        日志写入服务
     */
    public InternalOrderAccountServiceImpl(
            UserAccountMapper userAccountMapper,
            AccountTransactionMapper accountTransactionMapper,
            GoalhubLogService goalhubLogService) {
        this.userAccountMapper = userAccountMapper;
        this.accountTransactionMapper = accountTransactionMapper;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 扣减默认 USDT 账户余额。
     *
     * @param request 默认 USDT 账户扣减参数
     * @return 默认账户扣减结果
     */
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

        AccountTransactionEntity exists =
                accountTransactionMapper.selectOne(
                        Wrappers.lambdaQuery(AccountTransactionEntity.class)
                                .eq(AccountTransactionEntity::getBizType, BIZ_TYPE_BET_ORDER)
                                .eq(AccountTransactionEntity::getBizId, request.getBizId())
                                .last("LIMIT 1")
                );

        if (exists != null) {
            DeductDefaultAccountResponse response =
                    new DeductDefaultAccountResponse();

            response.setAccountId(account.getId());
            response.setCurrencyCode(account.getCurrencyCode());
            response.setBalanceBefore(exists.getBeforeBalance());
            response.setBalanceAfter(exists.getAfterBalance());

            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "DEDUCT_DEFAULT_USDT_IDEMPOTENT",
                    "订单扣减默认 USDT 账户幂等返回，accountId=" + account.getId()
                            + ", bizId=" + request.getBizId()
            );
            return response;
        }

        BigDecimal beforeBalance = safeAmount(account.getBalance());
        BigDecimal afterBalance = beforeBalance.subtract(request.getAmount());

        if (afterBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.BALANCE_NOT_ENOUGH);
        }

        account.setBalance(afterBalance);

        int affectedRows = userAccountMapper.updateById(account);

        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

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

        try {
            accountTransactionMapper.insert(transaction);
        } catch (DuplicateKeyException e) {
            account.setBalance(beforeBalance);
            userAccountMapper.updateById(account);

            AccountTransactionEntity duplicate =
                    accountTransactionMapper.selectOne(
                            Wrappers.lambdaQuery(AccountTransactionEntity.class)
                                    .eq(AccountTransactionEntity::getBizType, BIZ_TYPE_BET_ORDER)
                                    .eq(AccountTransactionEntity::getBizId, request.getBizId())
                                    .last("LIMIT 1")
                    );

            DeductDefaultAccountResponse response =
                    new DeductDefaultAccountResponse();

            response.setAccountId(account.getId());
            response.setCurrencyCode(account.getCurrencyCode());
            response.setBalanceBefore(
                    duplicate == null ? beforeBalance : duplicate.getBeforeBalance()
            );
            response.setBalanceAfter(
                    duplicate == null ? beforeBalance : duplicate.getAfterBalance()
            );

            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "DEDUCT_DEFAULT_USDT_DUPLICATE_BIZ_ID",
                    "订单扣减默认 USDT 账户命中唯一幂等，accountId=" + account.getId()
                            + ", bizId=" + request.getBizId()
            );
            return response;
        }

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
                        + ", amount=" + request.getAmount()
                        + ", beforeBalance=" + beforeBalance
                        + ", afterBalance=" + afterBalance
        );
        return response;
    }

    /**
     * 校验默认账户扣款请求。
     *
     * @param request 默认账户扣款请求
     */
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

    /**
     * 获取安全金额，空值按 0 处理。
     *
     * @param amount 原始金额
     * @return 非空金额
     */
    private BigDecimal safeAmount(
            BigDecimal amount) {

        if (amount == null) {
            return BigDecimal.ZERO;
        }

        return amount;
    }
}
