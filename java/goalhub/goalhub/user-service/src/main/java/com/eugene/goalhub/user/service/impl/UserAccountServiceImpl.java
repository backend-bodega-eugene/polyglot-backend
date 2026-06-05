package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.user.entity.AccountTransactionEntity;
import com.eugene.goalhub.user.entity.UserAccountEntity;
import com.eugene.goalhub.user.mapper.AccountTransactionMapper;
import com.eugene.goalhub.user.mapper.UserAccountMapper;
import com.eugene.goalhub.user.service.UserAccountService;
import dto.*;
import exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户账户服务实现。
 *
 * <p>负责前端账户查询、账户流水查询、后台账户分页、后台账户流水分页和后台余额维护。</p>
 */
@Service
public class UserAccountServiceImpl
        implements UserAccountService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "用户账户";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认和最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

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
     * 创建用户账户服务实现。
     *
     * @param userAccountMapper        用户账户 Mapper
     * @param accountTransactionMapper 账户流水 Mapper
     * @param goalhubLogService        日志写入服务
     */
    public UserAccountServiceImpl(
            UserAccountMapper userAccountMapper,
            AccountTransactionMapper accountTransactionMapper,
            GoalhubLogService goalhubLogService) {

        this.userAccountMapper = userAccountMapper;
        this.accountTransactionMapper = accountTransactionMapper;
        this.goalhubLogService = goalhubLogService;
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

        List<UserAccountResponse> responses = accounts.stream().map(account -> {

            UserAccountResponse response =
                    new UserAccountResponse();

            BigDecimal balance = safeAmount(account.getBalance());
            BigDecimal frozenBalance = safeAmount(account.getFrozenBalance());

            response.setAccountId(account.getId());
            response.setCurrencyCode(account.getCurrencyCode());
            response.setBalance(balance);
            response.setFrozenBalance(frozenBalance);
            response.setStatus(account.getStatus());
            response.setAvailableBalance(balance.subtract(frozenBalance));

            return response;

        }).collect(Collectors.toList());
        goalhubLogService.sysLog(
                MODULE_NAME,
                "GET_MY_ACCOUNTS",
                "查询当前用户账户列表，userId=" + userId + ", resultCount=" + responses.size()
        );
        return responses;
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

        if (request == null) {
            request = new AccountTransactionPageRequest();
        }
        initPage(request);

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
                            response.setBeforeBalance(safeAmount(item.getBeforeBalance()));
                            response.setAfterBalance(safeAmount(item.getAfterBalance()));
                            response.setRemark(item.getRemark());
                            response.setCreatedAt(item.getCreatedAt());

                            return response;

                        }).toList();

        goalhubLogService.sysLog(
                MODULE_NAME,
                "PAGE_MY_TRANSACTIONS",
                "分页查询当前用户账户流水，userId=" + userId
                        + ", pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", total=" + result.getTotal()
        );
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

        if (request == null) {
            request = new AdminUserAccountPageRequest();
        }
        initPage(request);

        Page<AdminUserAccountResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<AdminUserAccountResponse> result =
                userAccountMapper.adminAccountPage(page, request);

        goalhubLogService.sysLog(
                MODULE_NAME,
                "ADMIN_ACCOUNT_PAGE",
                "分页查询后台用户账户，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", username=" + request.getUsername()
                        + ", currencyCode=" + request.getCurrencyCode()
                        + ", total=" + result.getTotal()
        );
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

        if (request == null) {
            request = new AdminAccountTransactionPageRequest();
        }
        initPage(request);

        Page<AdminAccountTransactionResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<AdminAccountTransactionResponse> result =
                accountTransactionMapper.adminTransactionPage(page, request);

        goalhubLogService.sysLog(
                MODULE_NAME,
                "ADMIN_TRANSACTION_PAGE",
                "分页查询后台账户流水，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", username=" + request.getUsername()
                        + ", currencyCode=" + request.getCurrencyCode()
                        + ", total=" + result.getTotal()
        );
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
                request.getBizId(),
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

        if (request.getAmount() == null) {
            throw new BusinessException(ResultCode.ACCOUNT_CHANGE_AMOUNT_NOT_NULL);
        }

        changeBalance(
                request.getAccountId(),
                request.getAmount().negate(),
                "ADMIN_SUB",
                request.getBizId(),
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

        validateStatus(request.getStatus());

        UserAccountEntity account =
                userAccountMapper.selectByIdForUpdate(request.getAccountId());

        if (account == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }

        account.setStatus(request.getStatus());

        int affectedRows = userAccountMapper.updateById(account);

        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        goalhubLogService.bizLog(
                MODULE_NAME,
                "UPDATE_ACCOUNT_STATUS",
                account.getUserId(),
                null,
                "更新账户状态成功，accountId=" + account.getId()
                        + ", status=" + account.getStatus()
        );
    }

    /**
     * 调整账户余额并记录账户流水。
     *
     * @param accountId    账户 ID
     * @param changeAmount 余额变更金额
     * @param bizType      业务类型
     * @param bizId        业务 ID
     * @param remark       备注
     */
    private void changeBalance(
            Long accountId,
            BigDecimal changeAmount,
            String bizType,
            String bizId,
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

        String finalBizId = buildBizId(
                bizType,
                account.getId(),
                bizId
        );

        AccountTransactionEntity exists =
                accountTransactionMapper.selectOne(
                        Wrappers.lambdaQuery(AccountTransactionEntity.class)
                                .eq(AccountTransactionEntity::getBizType, bizType)
                                .eq(AccountTransactionEntity::getBizId, finalBizId)
                                .last("LIMIT 1")
                );

        if (exists != null) {
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "CHANGE_BALANCE_IDEMPOTENT",
                    "账户余额变更幂等返回，accountId=" + account.getId()
                            + ", bizType=" + bizType
                            + ", bizId=" + finalBizId
            );
            return;
        }

        BigDecimal beforeBalance = safeAmount(account.getBalance());
        BigDecimal afterBalance = beforeBalance.add(changeAmount);

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
        transaction.setBizType(bizType);
        transaction.setBizId(finalBizId);
        transaction.setChangeAmount(changeAmount);
        transaction.setBeforeBalance(beforeBalance);
        transaction.setAfterBalance(afterBalance);
        transaction.setRemark(remark);

        try {
            accountTransactionMapper.insert(transaction);
        } catch (DuplicateKeyException e) {
            account.setBalance(beforeBalance);
            userAccountMapper.updateById(account);
            goalhubLogService.sysLog(
                    MODULE_NAME,
                    "CHANGE_BALANCE_DUPLICATE_BIZ_ID",
                    "账户余额变更命中唯一幂等，accountId=" + account.getId()
                            + ", bizType=" + bizType
                            + ", bizId=" + finalBizId
            );
            return;
        }

        goalhubLogService.bizLog(
                MODULE_NAME,
                "CHANGE_BALANCE",
                account.getUserId(),
                null,
                "调整账户余额成功，accountId=" + account.getId()
                        + ", bizType=" + bizType
                        + ", changeAmount=" + changeAmount
                        + ", beforeBalance=" + beforeBalance
                        + ", afterBalance=" + afterBalance
        );
    }

    /**
     * 构建账户流水业务 ID。
     *
     * @param bizType   业务类型
     * @param accountId 账户 ID
     * @param bizId     请求业务 ID
     * @return 业务 ID
     */
    private String buildBizId(
            String bizType,
            Long accountId,
            String bizId) {

        if (bizId != null && !bizId.isBlank()) {
            return bizId;
        }

        return bizType + "_" + accountId + "_" + System.currentTimeMillis();
    }

    /**
     * 初始化前端账户流水分页参数。
     *
     * @param request 分页请求
     */
    private void initPage(AccountTransactionPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
            return;
        }

        if (request.getPageSize() > DEFAULT_PAGE_SIZE) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 初始化后台账户分页参数。
     *
     * @param request 分页请求
     */
    private void initPage(AdminUserAccountPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
            return;
        }

        if (request.getPageSize() > DEFAULT_PAGE_SIZE) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 初始化后台账户流水分页参数。
     *
     * @param request 分页请求
     */
    private void initPage(AdminAccountTransactionPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
            return;
        }

        if (request.getPageSize() > DEFAULT_PAGE_SIZE) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 校验账户状态。
     *
     * @param status 账户状态
     */
    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 获取安全金额，空值按 0 处理。
     *
     * @param amount 原始金额
     * @return 非空金额
     */
    private BigDecimal safeAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        return amount;
    }
}
