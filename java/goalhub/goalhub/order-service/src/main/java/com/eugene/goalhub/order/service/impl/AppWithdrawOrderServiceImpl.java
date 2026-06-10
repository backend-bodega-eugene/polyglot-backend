package com.eugene.goalhub.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.client.OrderUserSecurityClient;
import com.eugene.goalhub.order.entity.WithdrawOrderEntity;
import com.eugene.goalhub.order.mapper.WithdrawOrderMapper;
import com.eugene.goalhub.order.service.AppWithdrawOrderService;
import com.eugene.goalhub.order.service.OrderUserAccountService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.Result;
import response.ResultCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * 前端提现订单服务实现。
 *
 * <p>负责创建提现申请、冻结用户余额、校验提现参数并查询用户提现订单列表。</p>
 */
@Service
public class AppWithdrawOrderServiceImpl
        implements AppWithdrawOrderService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "前端提现订单";

    /**
     * 默认提现币种编码。
     */
    private static final String DEFAULT_CURRENCY_CODE = "USDT";

    /**
     * 待审核订单状态。
     */
    private static final String STATUS_PENDING = "PENDING";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * USDT 金额统一保留 4 位小数。
     */
    private static final int MONEY_SCALE = 4;

    /**
     * 提现订单 Mapper。
     */
    private final WithdrawOrderMapper withdrawOrderMapper;

    /**
     * 订单账户服务。
     */
    private final OrderUserAccountService orderUserAccountService;

    /**
     * 业务日志服务。
     */
    private final GoalhubLogService goalhubLogService;
    private final OrderUserSecurityClient orderUserSecurityClient;

    /**
     * 创建前端提现订单服务实例。
     *
     * @param withdrawOrderMapper     提现订单 Mapper
     * @param orderUserAccountService 订单账户服务
     * @param goalhubLogService       业务日志服务
     */
    public AppWithdrawOrderServiceImpl(
            WithdrawOrderMapper withdrawOrderMapper,
            OrderUserAccountService orderUserAccountService,
            OrderUserSecurityClient orderUserSecurityClient,
            GoalhubLogService goalhubLogService) {
        this.withdrawOrderMapper = withdrawOrderMapper;
        this.orderUserAccountService = orderUserAccountService;
        this.orderUserSecurityClient = orderUserSecurityClient;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 创建提现订单并冻结用户默认账户余额。
     *
     * @param userId  用户 ID
     * @param request 提现申请参数
     * @return 创建后的提现订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppWithdrawOrderResponse create(
            Long userId,
            AppWithdrawOrderCreateRequest request) {

        checkUserId(userId);

        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        checkAmount(request.getAmount());

        if (request.getWithdrawAddress() == null
                || request.getWithdrawAddress().isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        VerifyFundPasswordRequest verifyFundPasswordRequest = new VerifyFundPasswordRequest();
        verifyFundPasswordRequest.setUserId(userId);
        verifyFundPasswordRequest.setFundPassword(request.getFundPassword());

        Result<Void> verifyResult = orderUserSecurityClient.verifyFundPassword(verifyFundPasswordRequest);
        if (verifyResult == null || verifyResult.getCode() != ResultCode.SUCCESS.getCode()) {
            throw new BusinessException(ResultCode.FUND_PASSWORD_ERROR);
        }
        String currencyCode = normalizeCurrencyCode(request.getCurrencyCode());

        if (!DEFAULT_CURRENCY_CODE.equals(currencyCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal amount = normalizeMoney(request.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal feeAmount = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN);
        BigDecimal actualAmount = amount.subtract(feeAmount)
                .setScale(MONEY_SCALE, RoundingMode.DOWN);

        if (actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        String orderNo = generateOrderNo();

        DefaultAccountBalanceChangeRequest freezeRequest =
                new DefaultAccountBalanceChangeRequest();

        freezeRequest.setUserId(userId);
        freezeRequest.setAmount(amount);
        freezeRequest.setBizId(orderNo);
        freezeRequest.setRemark("用户提交提现申请，冻结余额，订单号：" + orderNo);

        orderUserAccountService.freezeDefaultUsdt(freezeRequest);

        WithdrawOrderEntity order = new WithdrawOrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCurrencyCode(currencyCode);
        order.setAmount(amount);
        order.setFeeAmount(feeAmount);
        order.setActualAmount(actualAmount);
        order.setStatus(STATUS_PENDING);
        order.setChainType(request.getChainType());
        order.setWithdrawAddress(request.getWithdrawAddress());
        order.setRemark(request.getRemark());

        withdrawOrderMapper.insert(order);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "CREATE_WITHDRAW_ORDER",
                userId,
                null,
                "用户提交提现申请成功，orderNo=" + order.getOrderNo()
                        + ", amount=" + amount
                        + ", actualAmount=" + actualAmount
        );

        return toResponse(order);
    }

    /**
     * 分页查询当前用户提现订单。
     *
     * @param userId  用户 ID
     * @param request 提现订单分页查询参数
     * @return 提现订单分页结果
     */
    @Override
    public PageResponse<AppWithdrawOrderResponse> page(
            Long userId,
            AppWithdrawOrderPageRequest request) {

        checkUserId(userId);

        if (request == null) {
            request = new AppWithdrawOrderPageRequest();
        }

        initPage(request);

        Page<AppWithdrawOrderResponse> page =
                new Page<>(request.getPageIndex(), request.getPageSize());

        Page<AppWithdrawOrderResponse> resultPage =
                withdrawOrderMapper.selectAppPage(page, userId, request);

        if (resultPage.getRecords() != null) {
            resultPage.getRecords().forEach(this::normalizeResponse);
        }

        return new PageResponse<>(
                resultPage.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                resultPage.getRecords()
        );
    }

    /**
     * 校验用户 ID。
     *
     * @param userId 用户 ID
     */
    private void checkUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.USER_ID_NOT_NULL);
        }
    }

    /**
     * 校验提现金额。
     *
     * @param amount 提现金额
     */
    private void checkAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 规范化 USDT 金额，小数位不足时补 0。
     *
     * @param amount 原始金额
     * @return 4 位小数金额
     */
    private BigDecimal normalizeMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.DOWN);
    }

    /**
     * 规范化币种编码。
     *
     * @param currencyCode 原始币种编码
     * @return 规范化后的币种编码
     */
    private String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return DEFAULT_CURRENCY_CODE;
        }

        return currencyCode.trim().toUpperCase();
    }

    /**
     * 初始化分页参数。
     *
     * @param request 提现订单分页查询参数
     */
    private void initPage(AppWithdrawOrderPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 生成提现订单号。
     *
     * @return 提现订单号
     */
    private String generateOrderNo() {
        return "WD" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 转换提现订单响应。
     *
     * @param entity 提现订单实体
     * @return 前端提现订单响应
     */
    private AppWithdrawOrderResponse toResponse(WithdrawOrderEntity entity) {
        AppWithdrawOrderResponse response = new AppWithdrawOrderResponse();
        response.setId(entity.getId());
        response.setOrderNo(entity.getOrderNo());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setAmount(normalizeNullableMoney(entity.getAmount()));
        response.setActualAmount(normalizeNullableMoney(entity.getActualAmount()));
        response.setFeeAmount(normalizeNullableMoney(entity.getFeeAmount()));
        response.setStatus(entity.getStatus());
        response.setChainType(entity.getChainType());
        response.setWithdrawAddress(entity.getWithdrawAddress());
        response.setTxHash(entity.getTxHash());
        response.setRemark(entity.getRemark());
        response.setAuditRemark(entity.getAuditRemark());
        response.setAuditTime(entity.getAuditTime());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private void normalizeResponse(
            AppWithdrawOrderResponse response) {

        response.setAmount(normalizeNullableMoney(response.getAmount()));
        response.setActualAmount(normalizeNullableMoney(response.getActualAmount()));
        response.setFeeAmount(normalizeNullableMoney(response.getFeeAmount()));
    }

    private BigDecimal normalizeNullableMoney(
            BigDecimal amount) {

        if (amount == null) {
            return null;
        }

        return normalizeMoney(amount);
    }
}
