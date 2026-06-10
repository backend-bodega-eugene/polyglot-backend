package com.eugene.goalhub.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.entity.WithdrawOrderEntity;
import com.eugene.goalhub.order.mapper.WithdrawOrderMapper;
import com.eugene.goalhub.order.service.AdminWithdrawOrderService;
import com.eugene.goalhub.order.service.OrderUserAccountService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 后台提现订单服务实现。
 *
 * <p>负责后台提现订单分页查询、详情查询、审核扣减或解冻余额，并记录审核日志。</p>
 */
@Service
public class AdminWithdrawOrderServiceImpl
        implements AdminWithdrawOrderService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "提现订单管理";

    /**
     * 待审核订单状态。
     */
    private static final String STATUS_PENDING = "PENDING";

    /**
     * 审核通过订单状态。
     */
    private static final String STATUS_APPROVED = "APPROVED";

    /**
     * 审核拒绝订单状态。
     */
    private static final String STATUS_REJECTED = "REJECTED";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

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

    /**
     * 创建后台提现订单服务实例。
     *
     * @param withdrawOrderMapper     提现订单 Mapper
     * @param orderUserAccountService 订单账户服务
     * @param goalhubLogService       业务日志服务
     */
    public AdminWithdrawOrderServiceImpl(
            WithdrawOrderMapper withdrawOrderMapper,
            OrderUserAccountService orderUserAccountService,
            GoalhubLogService goalhubLogService) {

        this.withdrawOrderMapper = withdrawOrderMapper;
        this.orderUserAccountService = orderUserAccountService;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 分页查询后台提现订单。
     *
     * @param request 提现订单分页查询参数
     * @return 提现订单分页结果
     */
    @Override
    public PageResponse<AdminWithdrawOrderResponse> page(
            AdminWithdrawOrderPageRequest request) {

        if (request == null) {
            request = new AdminWithdrawOrderPageRequest();
        }

        initPage(request);

        Page<AdminWithdrawOrderResponse> page =
                new Page<>(request.getPageIndex(), request.getPageSize());

        Page<AdminWithdrawOrderResponse> resultPage =
                withdrawOrderMapper.selectAdminPage(page, request);

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
     * 查询提现订单详情。
     *
     * @param request 提现订单详情查询参数
     * @return 提现订单详情
     */
    @Override
    public AdminWithdrawOrderResponse detail(
            AdminWithdrawOrderDetailRequest request) {

        if (request == null || request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        WithdrawOrderEntity entity =
                withdrawOrderMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        return toResponse(entity);
    }

    /**
     * 审核提现订单。
     *
     * <p>审核通过时确认扣减冻结余额，审核拒绝时解冻用户余额。</p>
     *
     * @param request       提现订单审核参数
     * @param adminId       审核管理员 ID
     * @param adminUsername 审核管理员用户名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(
            AdminWithdrawOrderAuditRequest request,
            Long adminId,
            String adminUsername) {

        if (request == null || request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        validateAuditStatus(request.getAuditStatus());

        WithdrawOrderEntity order =
                withdrawOrderMapper.selectByIdForUpdate(request.getId());

        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal amount = order.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        amount = normalizeMoney(amount);

        if (STATUS_APPROVED.equals(request.getAuditStatus())) {
            DefaultAccountBalanceChangeRequest balanceRequest =
                    new DefaultAccountBalanceChangeRequest();

            balanceRequest.setUserId(order.getUserId());
            balanceRequest.setAmount(amount);
            balanceRequest.setBizId(order.getOrderNo());
            balanceRequest.setRemark("提现审核通过，扣减冻结金额，订单号：" + order.getOrderNo());

            orderUserAccountService.confirmFrozenDefaultUsdt(balanceRequest);
        }

        if (STATUS_REJECTED.equals(request.getAuditStatus())) {
            DefaultAccountBalanceChangeRequest balanceRequest =
                    new DefaultAccountBalanceChangeRequest();

            balanceRequest.setUserId(order.getUserId());
            balanceRequest.setAmount(amount);
            balanceRequest.setBizId(order.getOrderNo());
            balanceRequest.setRemark("提现审核拒绝，解冻金额，订单号：" + order.getOrderNo());

            orderUserAccountService.unfreezeDefaultUsdt(balanceRequest);
        }

        order.setStatus(request.getAuditStatus());
        order.setAuditRemark(request.getAuditRemark());
        order.setAuditAdminId(adminId);
        order.setAuditAdminName(adminUsername);
        order.setAuditTime(LocalDateTime.now());

        int affectedRows = withdrawOrderMapper.updateById(order);

        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        goalhubLogService.bizLog(
                MODULE_NAME,
                "AUDIT_WITHDRAW_ORDER",
                adminId,
                adminUsername,
                "审核提现订单，orderNo=" + order.getOrderNo()
                        + ", userId=" + order.getUserId()
                        + ", amount=" + amount
                        + ", status=" + request.getAuditStatus()
        );
    }

    /**
     * 校验审核状态。
     *
     * @param auditStatus 审核状态
     */
    private void validateAuditStatus(String auditStatus) {
        if (STATUS_APPROVED.equals(auditStatus)
                || STATUS_REJECTED.equals(auditStatus)) {
            return;
        }

        throw new BusinessException(ResultCode.PARAM_ERROR);
    }

    /**
     * 初始化后台分页参数。
     *
     * @param request 提现订单分页查询参数
     */
    private void initPage(AdminWithdrawOrderPageRequest request) {
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
     * 转换后台提现订单响应。
     *
     * @param entity 提现订单实体
     * @return 后台提现订单响应
     */
    private AdminWithdrawOrderResponse toResponse(WithdrawOrderEntity entity) {
        AdminWithdrawOrderResponse response = new AdminWithdrawOrderResponse();
        response.setId(entity.getId());
        response.setOrderNo(entity.getOrderNo());
        response.setUserId(entity.getUserId());
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
        response.setAuditAdminId(entity.getAuditAdminId());
        response.setAuditAdminName(entity.getAuditAdminName());
        response.setAuditTime(entity.getAuditTime());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private void normalizeResponse(
            AdminWithdrawOrderResponse response) {

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

    private BigDecimal normalizeMoney(
            BigDecimal amount) {

        return amount.setScale(MONEY_SCALE, RoundingMode.DOWN);
    }
}
