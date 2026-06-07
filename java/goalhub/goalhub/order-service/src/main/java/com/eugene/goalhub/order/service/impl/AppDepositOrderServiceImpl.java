package com.eugene.goalhub.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.entity.DepositOrderEntity;
import com.eugene.goalhub.order.mapper.DepositOrderMapper;
import com.eugene.goalhub.order.service.AppDepositOrderService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class AppDepositOrderServiceImpl
        implements AppDepositOrderService {

    private static final String MODULE_NAME = "前端充值订单";

    private static final String DEFAULT_CURRENCY_CODE = "USDT";

    private static final String STATUS_PENDING = "PENDING";

    private static final int DEFAULT_PAGE_INDEX = 1;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final DepositOrderMapper depositOrderMapper;

    private final GoalhubLogService goalhubLogService;

    public AppDepositOrderServiceImpl(
            DepositOrderMapper depositOrderMapper,
            GoalhubLogService goalhubLogService) {
        this.depositOrderMapper = depositOrderMapper;
        this.goalhubLogService = goalhubLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppDepositOrderResponse create(
            Long userId,
            AppDepositOrderCreateRequest request) {

        checkUserId(userId);
        checkAmount(request == null ? null : request.getAmount());

        String currencyCode = normalizeCurrencyCode(request.getCurrencyCode());

        BigDecimal amount = request.getAmount()
                .setScale(2, RoundingMode.DOWN);

        DepositOrderEntity order = new DepositOrderEntity();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setCurrencyCode(currencyCode);
        order.setAmount(amount);
        order.setActualAmount(amount);
        order.setStatus(STATUS_PENDING);
        order.setChainType(request.getChainType());
        order.setTxHash(request.getTxHash());
        order.setRemark(request.getRemark());

        depositOrderMapper.insert(order);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "CREATE_DEPOSIT_ORDER",
                userId,
                null,
                "用户提交充值申请成功，orderNo=" + order.getOrderNo()
                        + ", amount=" + amount
                        + ", currencyCode=" + currencyCode
        );

        return toResponse(order);
    }

    @Override
    public PageResponse<AppDepositOrderResponse> page(
            Long userId,
            AppDepositOrderPageRequest request) {

        checkUserId(userId);

        if (request == null) {
            request = new AppDepositOrderPageRequest();
        }

        initPage(request);

        Page<AppDepositOrderResponse> page =
                new Page<>(request.getPageIndex(), request.getPageSize());

        Page<AppDepositOrderResponse> resultPage =
                depositOrderMapper.selectAppPage(page, userId, request);

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

    private void initPage(AppDepositOrderPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    private String generateOrderNo() {
        return "DP" + UUID.randomUUID().toString().replace("-", "");
    }

    private AppDepositOrderResponse toResponse(DepositOrderEntity entity) {
        AppDepositOrderResponse response = new AppDepositOrderResponse();
        response.setId(entity.getId());
        response.setOrderNo(entity.getOrderNo());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setAmount(entity.getAmount());
        response.setActualAmount(entity.getActualAmount());
        response.setStatus(entity.getStatus());
        response.setChainType(entity.getChainType());
        response.setTxHash(entity.getTxHash());
        response.setRemark(entity.getRemark());
        response.setAuditRemark(entity.getAuditRemark());
        response.setAuditTime(entity.getAuditTime());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}