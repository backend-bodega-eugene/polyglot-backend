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
import java.time.LocalDateTime;

@Service
public class AdminWithdrawOrderServiceImpl
        implements AdminWithdrawOrderService {

    private static final String MODULE_NAME = "提现订单管理";

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private static final int DEFAULT_PAGE_INDEX = 1;
    private static final int DEFAULT_PAGE_SIZE = 100;

    private final WithdrawOrderMapper withdrawOrderMapper;

    private final OrderUserAccountService orderUserAccountService;

    private final GoalhubLogService goalhubLogService;

    public AdminWithdrawOrderServiceImpl(
            WithdrawOrderMapper withdrawOrderMapper,
            OrderUserAccountService orderUserAccountService,
            GoalhubLogService goalhubLogService) {

        this.withdrawOrderMapper = withdrawOrderMapper;
        this.orderUserAccountService = orderUserAccountService;
        this.goalhubLogService = goalhubLogService;
    }

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

        return new PageResponse<>(
                resultPage.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                resultPage.getRecords()
        );
    }

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

    private void validateAuditStatus(String auditStatus) {
        if (STATUS_APPROVED.equals(auditStatus)
                || STATUS_REJECTED.equals(auditStatus)) {
            return;
        }

        throw new BusinessException(ResultCode.PARAM_ERROR);
    }

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

    private AdminWithdrawOrderResponse toResponse(WithdrawOrderEntity entity) {
        AdminWithdrawOrderResponse response = new AdminWithdrawOrderResponse();
        response.setId(entity.getId());
        response.setOrderNo(entity.getOrderNo());
        response.setUserId(entity.getUserId());
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
        response.setAuditAdminId(entity.getAuditAdminId());
        response.setAuditAdminName(entity.getAuditAdminName());
        response.setAuditTime(entity.getAuditTime());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}