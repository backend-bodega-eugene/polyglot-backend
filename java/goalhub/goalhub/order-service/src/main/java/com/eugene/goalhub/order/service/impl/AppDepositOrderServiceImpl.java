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

/**
 * 前端充值订单服务实现。
 *
 * <p>负责创建用户充值申请、规范化充值币种、校验金额并查询用户充值订单列表。</p>
 */
@Service
public class AppDepositOrderServiceImpl
        implements AppDepositOrderService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "前端充值订单";

    /**
     * 默认充值币种编码。
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
     * 充值订单 Mapper。
     */
    private final DepositOrderMapper depositOrderMapper;

    /**
     * 业务日志服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建前端充值订单服务实例。
     *
     * @param depositOrderMapper 充值订单 Mapper
     * @param goalhubLogService  业务日志服务
     */
    public AppDepositOrderServiceImpl(
            DepositOrderMapper depositOrderMapper,
            GoalhubLogService goalhubLogService) {
        this.depositOrderMapper = depositOrderMapper;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 创建充值订单。
     *
     * @param userId  用户 ID
     * @param request 充值申请参数
     * @return 创建后的充值订单
     */
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

    /**
     * 分页查询当前用户充值订单。
     *
     * @param userId  用户 ID
     * @param request 充值订单分页查询参数
     * @return 充值订单分页结果
     */
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
     * 校验充值金额。
     *
     * @param amount 充值金额
     */
    private void checkAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
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
     * @param request 充值订单分页查询参数
     */
    private void initPage(AppDepositOrderPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 生成充值订单号。
     *
     * @return 充值订单号
     */
    private String generateOrderNo() {
        return "DP" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 转换充值订单响应。
     *
     * @param entity 充值订单实体
     * @return 前端充值订单响应
     */
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
