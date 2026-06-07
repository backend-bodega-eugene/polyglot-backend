package com.eugene.goalhub.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.entity.WithdrawOrderEntity;
import com.eugene.goalhub.order.mapper.WithdrawOrderMapper;
import com.eugene.goalhub.order.service.AppWithdrawOrderService;
import com.eugene.goalhub.order.service.OrderUserAccountService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class AppWithdrawOrderServiceImpl
        implements AppWithdrawOrderService {

    private static final String MODULE_NAME = "前端提现订单";

    private static final String DEFAULT_CURRENCY_CODE = "USDT";

    private static final String STATUS_PENDING = "PENDING";

    private static final int DEFAULT_PAGE_INDEX = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final WithdrawOrderMapper withdrawOrderMapper;

    private final OrderUserAccountService orderUserAccountService;

    private final GoalhubLogService goalhubLogService;

    public AppWithdrawOrderServiceImpl(
            WithdrawOrderMapper withdrawOrderMapper,
            OrderUserAccountService orderUserAccountService,
            GoalhubLogService goalhubLogService) {
        this.withdrawOrderMapper = withdrawOrderMapper;
        this.orderUserAccountService = orderUserAccountService;
        this.goalhubLogService = goalhubLogService;
    }

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

        String currencyCode = normalizeCurrencyCode(request.getCurrencyCode());

        if (!DEFAULT_CURRENCY_CODE.equals(currencyCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal amount = request.getAmount()
                .setScale(2, RoundingMode.DOWN);

        BigDecimal feeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
        BigDecimal actualAmount = amount.subtract(feeAmount);

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

        return new PageResponse<>(
                resultPage.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                resultPage.getRecords()
        );
    }

    private void checkUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.USER_ID_NOT_NULL);
        }
    }

    private void checkAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return DEFAULT_CURRENCY_CODE;
        }

        return currencyCode.trim().toUpperCase();
    }

    private void initPage(AppWithdrawOrderPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    private String generateOrderNo() {
        return "WD" + UUID.randomUUID().toString().replace("-", "");
    }

    private AppWithdrawOrderResponse toResponse(WithdrawOrderEntity entity) {
        AppWithdrawOrderResponse response = new AppWithdrawOrderResponse();
        response.setId(entity.getId());
        response.setOrderNo(entity.getOrderNo());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setAmount(entity.getAmount());
        response.setActualAmount(entity.getActualAmount());
        response.setFeeAmount(entity.getFeeAmount());
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
}