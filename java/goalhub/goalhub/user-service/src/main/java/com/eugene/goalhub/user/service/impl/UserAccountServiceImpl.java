package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.user.entity.AccountTransactionEntity;
import com.eugene.goalhub.user.entity.UserAccountEntity;
import com.eugene.goalhub.user.mapper.AccountTransactionMapper;
import com.eugene.goalhub.user.mapper.UserAccountMapper;
import com.eugene.goalhub.user.service.UserAccountService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户账户服务实现。
 */
@Service
public class UserAccountServiceImpl
        implements UserAccountService {

    private final UserAccountMapper userAccountMapper;

    private final AccountTransactionMapper accountTransactionMapper;

    public UserAccountServiceImpl(
            UserAccountMapper userAccountMapper,
            AccountTransactionMapper accountTransactionMapper) {

        this.userAccountMapper = userAccountMapper;
        this.accountTransactionMapper = accountTransactionMapper;
    }

    /**
     * 查询当前用户账户列表。
     *
     * @param userId 用户 ID
     * @return 用户账户列表
     */
    @Override
    public List<UserAccountResponse> getMyAccounts(Long userId) {

        List<UserAccountEntity> accounts =
                userAccountMapper.selectList(
                        Wrappers.lambdaQuery(UserAccountEntity.class)
                                .eq(UserAccountEntity::getUserId, userId)
                );

        return accounts.stream().map(account -> {

            UserAccountResponse response =
                    new UserAccountResponse();

            response.setAccountId(account.getId());
            response.setCurrencyCode(account.getCurrencyCode());
            response.setBalance(account.getBalance());
            response.setFrozenBalance(account.getFrozenBalance());
            response.setStatus(account.getStatus());

            response.setAvailableBalance(
                    account.getBalance()
                            .subtract(account.getFrozenBalance())
            );

            return response;

        }).collect(Collectors.toList());
    }

    /**
     * 分页查询当前用户账户流水。
     *
     * @param userId  用户 ID
     * @param request 账户流水分页查询条件
     * @return 账户流水分页数据
     */
    @Override
    public PageResponse<AccountTransactionResponse>
    pageMyTransactions(
            Long userId,
            AccountTransactionPageRequest request) {

        Page<AccountTransactionEntity> page =
                new Page<>(
                        request.getPageIndex(),
                        request.getPageSize()
                );

        LambdaQueryWrapper<AccountTransactionEntity> query =
                Wrappers.lambdaQuery(AccountTransactionEntity.class)
                        .eq(AccountTransactionEntity::getUserId, userId)
                        .orderByDesc(AccountTransactionEntity::getCreatedAt);

        if (request.getCurrencyCode() != null
                && !request.getCurrencyCode().isBlank()) {

            query.eq(
                    AccountTransactionEntity::getCurrencyCode,
                    request.getCurrencyCode()
            );
        }

        if (request.getBizType() != null
                && !request.getBizType().isBlank()) {

            query.eq(
                    AccountTransactionEntity::getBizType,
                    request.getBizType()
            );
        }

        if (request.getStartTime() != null) {

            query.ge(
                    AccountTransactionEntity::getCreatedAt,
                    request.getStartTime()
            );
        }

        if (request.getEndTime() != null) {

            query.le(
                    AccountTransactionEntity::getCreatedAt,
                    request.getEndTime()
            );
        }

        Page<AccountTransactionEntity> result =
                accountTransactionMapper.selectPage(
                        page,
                        query
                );

        List<AccountTransactionResponse> records =
                result.getRecords()
                        .stream()
                        .map(item -> {

                            AccountTransactionResponse response =
                                    new AccountTransactionResponse();

                            response.setId(item.getId());
                            response.setCurrencyCode(item.getCurrencyCode());
                            response.setBizType(item.getBizType());
                            response.setBizId(item.getBizId());
                            response.setChangeAmount(item.getChangeAmount());
                            response.setBeforeBalance(item.getBeforeBalance());
                            response.setAfterBalance(item.getAfterBalance());
                            response.setRemark(item.getRemark());
                            response.setCreatedAt(item.getCreatedAt());

                            return response;

                        }).toList();

        return new PageResponse<>(
                result.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                records
        );
    }

    /**
     * 分页查询后台用户账户列表。
     *
     * @param request 后台用户账户分页查询条件
     * @return 后台用户账户分页数据
     */
    @Override
    public PageResponse<AdminUserAccountResponse> adminAccountPage(
            AdminUserAccountPageRequest request) {

        Page<AdminUserAccountResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<AdminUserAccountResponse> result =
                userAccountMapper.adminAccountPage(page, request);

        return new PageResponse<>(
                result.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                result.getRecords()
        );
    }

    /**
     * 分页查询后台账户流水列表。
     *
     * @param request 后台账户流水分页查询条件
     * @return 后台账户流水分页数据
     */
    @Override
    public PageResponse<AdminAccountTransactionResponse> adminTransactionPage(
            AdminAccountTransactionPageRequest request) {

        Page<AdminAccountTransactionResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<AdminAccountTransactionResponse> result =
                accountTransactionMapper.adminTransactionPage(page, request);

        return new PageResponse<>(
                result.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                result.getRecords()
        );
    }

    /**
     * 后台增加账户余额。
     *
     * @param request 账户余额增加参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminAddBalance(
            AdminAccountBalanceChangeRequest request) {

        changeBalance(
                request.getAccountId(),
                request.getAmount(),
                "ADMIN_ADD",
                request.getRemark()
        );
    }

    /**
     * 后台扣减账户余额。
     *
     * @param request 账户余额扣减参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminSubBalance(
            AdminAccountBalanceChangeRequest request) {

        changeBalance(
                request.getAccountId(),
                request.getAmount().negate(),
                "ADMIN_SUB",
                request.getRemark()
        );
    }

    /**
     * 后台更新账户状态。
     *
     * @param request 账户状态更新参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateStatus(
            AdminAccountStatusUpdateRequest request) {

        UserAccountEntity account =
                userAccountMapper.selectById(request.getAccountId());

        if (account == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        account.setStatus(request.getStatus());

        userAccountMapper.updateById(account);
    }

    /**
     * 调整账户余额并记录账户流水。
     *
     * @param accountId    账户 ID
     * @param changeAmount 余额变更金额
     * @param bizType      业务类型
     * @param remark       备注
     */
    private void changeBalance(
            Long accountId,
            BigDecimal changeAmount,
            String bizType,
            String remark) {

        if (changeAmount == null
                || changeAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        UserAccountEntity account =
                userAccountMapper.selectByIdForUpdate(accountId);

        if (account == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        BigDecimal beforeBalance = account.getBalance();
        BigDecimal afterBalance = beforeBalance.add(changeAmount);

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
        transaction.setBizType(bizType);
        transaction.setBizId(
                bizType + "_" + account.getId() + "_" + System.currentTimeMillis()
        );
        transaction.setChangeAmount(changeAmount);
        transaction.setBeforeBalance(beforeBalance);
        transaction.setAfterBalance(afterBalance);
        transaction.setRemark(remark);

        accountTransactionMapper.insert(transaction);
    }
}
