package com.eugene.goalhub.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.entity.DepositOrderEntity;
import com.eugene.goalhub.order.mapper.DepositOrderMapper;
import com.eugene.goalhub.order.service.AdminDepositOrderService;
import com.eugene.goalhub.order.service.OrderUserAccountService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AdminDepositOrderServiceImpl
        implements AdminDepositOrderService {

    private static final String MODULE_NAME = "充值订单管理";

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private static final int DEFAULT_PAGE_INDEX = 1;
    private static final int DEFAULT_PAGE_SIZE = 100;

    private final DepositOrderMapper depositOrderMapper;

    private final OrderUserAccountService orderUserAccountService;

    private final GoalhubLogService goalhubLogService;

    public AdminDepositOrderServiceImpl(
            DepositOrderMapper depositOrderMapper,
            OrderUserAccountService orderUserAccountService,
            GoalhubLogService goalhubLogService) {

        this.depositOrderMapper = depositOrderMapper;
        this.orderUserAccountService = orderUserAccountService;
        this.goalhubLogService = goalhubLogService;
    }

    @Override
    public PageResponse<AdminDepositOrderResponse> page(
            AdminDepositOrderPageRequest request) {

        if (request == null) {
            request = new AdminDepositOrderPageRequest();
        }

        initPage(request);

        Page<AdminDepositOrderResponse> page =
                new Page<>(request.getPageIndex(), request.getPageSize());

        Page<AdminDepositOrderResponse> resultPage =
                depositOrderMapper.selectAdminPage(page, request);

        return new PageResponse<>(
                resultPage.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                resultPage.getRecords()
        );
    }

    @Override
    public AdminDepositOrderResponse detail(
            AdminDepositOrderDetailRequest request) {

        if (request == null || request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        DepositOrderEntity entity =
                depositOrderMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        return toResponse(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(
            AdminDepositOrderAuditRequest request,
            Long adminId,
            String adminUsername) {

        if (request == null || request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        validateAuditStatus(request.getAuditStatus());

        DepositOrderEntity order =
                depositOrderMapper.selectByIdForUpdate(request.getId());

        if (order == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        BigDecimal actualAmount = order.getActualAmount();

        if (actualAmount == null) {
            actualAmount = order.getAmount();
        }

        if (actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (STATUS_APPROVED.equals(request.getAuditStatus())) {
            DefaultAccountBalanceChangeRequest balanceRequest =
                    new DefaultAccountBalanceChangeRequest();

            balanceRequest.setUserId(order.getUserId());
            balanceRequest.setAmount(actualAmount);
            balanceRequest.setBizId(order.getOrderNo());
            balanceRequest.setRemark("充值审核通过，订单号：" + order.getOrderNo());

            orderUserAccountService.addDefaultUsdt(balanceRequest);
        }

        order.setStatus(request.getAuditStatus());
        order.setActualAmount(actualAmount);
        order.setAuditRemark(request.getAuditRemark());
        order.setAuditAdminId(adminId);
        order.setAuditAdminName(adminUsername);
        order.setAuditTime(LocalDateTime.now());

        int affectedRows = depositOrderMapper.updateById(order);

        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        goalhubLogService.bizLog(
                MODULE_NAME,
                "AUDIT_DEPOSIT_ORDER",
                adminId,
                adminUsername,
                "审核充值订单，orderNo=" + order.getOrderNo()
                        + ", userId=" + order.getUserId()
                        + ", amount=" + actualAmount
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

    private void initPage(AdminDepositOrderPageRequest request) {
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

    private AdminDepositOrderResponse toResponse(DepositOrderEntity entity) {
        AdminDepositOrderResponse response = new AdminDepositOrderResponse();
        response.setId(entity.getId());
        response.setOrderNo(entity.getOrderNo());
        response.setUserId(entity.getUserId());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setAmount(entity.getAmount());
        response.setActualAmount(entity.getActualAmount());
        response.setStatus(entity.getStatus());
        response.setChainType(entity.getChainType());
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