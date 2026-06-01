package com.eugene.goalhub.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.entity.BetOrderEntity;
import com.eugene.goalhub.order.mapper.BetOrderItemMapper;
import com.eugene.goalhub.order.mapper.BetOrderMapper;
import com.eugene.goalhub.order.service.AdminBetOrderService;
import com.eugene.goalhub.order.service.OrderUserAccountService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台投注订单管理服务实现。
 */
@Service
public class AdminBetOrderServiceImpl
        implements AdminBetOrderService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "投注订单管理";

    /**
     * 待判定订单状态。
     */
    private static final String STATUS_PENDING = "PENDING";

    /**
     * 赢单状态。
     */
    private static final String STATUS_WIN = "WIN";

    /**
     * 输单状态。
     */
    private static final String STATUS_LOSE = "LOSE";

    /**
     * 取消订单状态。
     */
    private static final String STATUS_CANCELLED = "CANCELLED";

    /**
     * 冻结订单状态。
     */
    private static final String STATUS_FROZEN = "FROZEN";

    /**
     * 退款订单状态。
     */
    private static final String STATUS_REFUNDED = "REFUNDED";

    /**
     * 已结算订单状态。
     */
    private static final String STATUS_SETTLED = "SETTLED";

    /**
     * 系统判定赢。
     */
    private static final String SYSTEM_WIN = "WIN";

    /**
     * 系统判定输。
     */
    private static final String SYSTEM_LOSE = "LOSE";

    /**
     * 系统判定取消。
     */
    private static final String SYSTEM_CANCELLED = "CANCELLED";

    /**
     * 系统判定退款。
     */
    private static final String SYSTEM_REFUNDED = "REFUNDED";

    /**
     * 投注订单 Mapper。
     */
    private final BetOrderMapper betOrderMapper;

    /**
     * 投注订单明细 Mapper。
     */
    private final BetOrderItemMapper betOrderItemMapper;

    /**
     * 用户账户服务。
     */
    private final OrderUserAccountService orderUserAccountService;

    /**
     * 业务日志服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建后台投注订单管理服务实现。
     *
     * @param betOrderMapper          投注订单 Mapper
     * @param betOrderItemMapper      投注订单明细 Mapper
     * @param orderUserAccountService 用户账户服务
     * @param goalhubLogService       业务日志服务
     */
    public AdminBetOrderServiceImpl(
            BetOrderMapper betOrderMapper,
            BetOrderItemMapper betOrderItemMapper,
            OrderUserAccountService orderUserAccountService,
            GoalhubLogService goalhubLogService) {

        this.betOrderMapper = betOrderMapper;
        this.betOrderItemMapper = betOrderItemMapper;
        this.orderUserAccountService = orderUserAccountService;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 分页查询投注订单。
     *
     * @param request 投注订单分页查询条件
     * @return 投注订单分页结果
     */
    @Override
    public PageResponse<AdminBetOrderResponse> orderPage(
            AdminBetOrderPageRequest request) {

        Integer pageIndex =
                request.getPageIndex() == null
                        ? 1
                        : request.getPageIndex();

        Integer pageSize =
                request.getPageSize() == null
                        ? 10
                        : request.getPageSize();

        Page<AdminBetOrderResponse> page =
                new Page<>(pageIndex, pageSize);

        Page<AdminBetOrderResponse> resultPage =
                betOrderMapper.selectAdminOrderPage(
                        page,
                        request
                );

        return new PageResponse<>(
                resultPage.getTotal(),
                pageIndex,
                pageSize,
                resultPage.getRecords()
        );
    }

    /**
     * 分页查询投注订单明细。
     *
     * @param request 投注订单明细分页查询条件
     * @return 投注订单明细分页结果
     */
    @Override
    public PageResponse<AdminBetOrderItemResponse> orderItemPage(
            AdminBetOrderItemPageRequest request) {

        if (request.getLangCode() == null
                || request.getLangCode().isBlank()) {

            request.setLangCode("zh-CN");
        }

        Integer pageIndex =
                request.getPageIndex() == null
                        ? 1
                        : request.getPageIndex();

        Integer pageSize =
                request.getPageSize() == null
                        ? 10
                        : request.getPageSize();

        Page<AdminBetOrderItemResponse> page =
                new Page<>(pageIndex, pageSize);

        Page<AdminBetOrderItemResponse> resultPage =
                betOrderItemMapper.selectAdminOrderItemPage(
                        page,
                        request
                );

        return new PageResponse<>(
                resultPage.getTotal(),
                pageIndex,
                pageSize,
                resultPage.getRecords()
        );
    }

    /**
     * 审核投注订单。
     *
     * @param request       投注订单审核参数
     * @param adminId       管理员 ID
     * @param adminUsername 管理员用户名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewOrder(
            AdminBetOrderReviewRequest request,
            Long adminId,
            String adminUsername) {

        requireRemark(request.getRemark());

        BetOrderEntity order =
                getOrderOrThrow(request.getOrderId());

        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BET_ORDER_ONLY_PENDING_CAN_REVIEW);
        }

        validateReviewResult(
                order.getSystemResult(),
                request.getReviewResult()
        );

        order.setStatus(request.getReviewResult());
        order.setReviewResult(request.getReviewResult());
        order.setReviewRemark(request.getRemark());
        order.setReviewAdminId(adminId);
        order.setReviewAdminName(adminUsername);
        order.setReviewedAt(LocalDateTime.now());

        betOrderMapper.updateById(order);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "审核投注订单",
                adminId,
                adminUsername,
                "订单号：" + order.getOrderNo()
                        + "，系统判定结果：" + order.getSystemResult()
                        + "，审核结果：" + request.getReviewResult()
                        + "，备注：" + request.getRemark()
        );
    }

    /**
     * 冻结投注订单。
     *
     * @param request       投注订单冻结参数
     * @param adminId       管理员 ID
     * @param adminUsername 管理员用户名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeOrder(
            AdminBetOrderFreezeRequest request,
            Long adminId,
            String adminUsername) {

        requireRemark(request.getRemark());

        BetOrderEntity order =
                getOrderOrThrow(request.getOrderId());

        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BET_ORDER_ONLY_PENDING_CAN_FREEZE);
        }

        order.setStatus(STATUS_FROZEN);
        order.setReviewResult(STATUS_FROZEN);
        order.setReviewRemark(request.getRemark());
        order.setReviewAdminId(adminId);
        order.setReviewAdminName(adminUsername);
        order.setReviewedAt(LocalDateTime.now());

        betOrderMapper.updateById(order);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "冻结投注订单",
                adminId,
                adminUsername,
                "订单号：" + order.getOrderNo()
                        + "，备注：" + request.getRemark()
        );
    }

    /**
     * 结算投注订单。
     *
     * @param request       投注订单结算参数
     * @param adminId       管理员 ID
     * @param adminUsername 管理员用户名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleOrder(
            AdminBetOrderSettleRequest request,
            Long adminId,
            String adminUsername) {

        requireRemark(request.getRemark());

        BetOrderEntity order =
                getOrderOrThrow(request.getOrderId());

        if (STATUS_SETTLED.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BET_ORDER_ALREADY_SETTLED);
        }

        BigDecimal settleAmount =
                calculateSettleAmount(order);

        if (settleAmount == null
                || settleAmount.compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessException(ResultCode.BET_ORDER_SETTLE_AMOUNT_INVALID);
        }

        if (settleAmount.compareTo(BigDecimal.ZERO) > 0) {

            AdminAccountBalanceChangeRequest balanceRequest =
                    new AdminAccountBalanceChangeRequest();

            balanceRequest.setAccountId(order.getAccountId());
            balanceRequest.setAmount(settleAmount);
            balanceRequest.setRemark(
                    "投注订单结算，订单号：" + order.getOrderNo()
                            + "，备注：" + request.getRemark()
            );

            orderUserAccountService.addBalance(balanceRequest);
        }

        order.setStatus(STATUS_SETTLED);
        order.setSettleAmount(settleAmount);
        order.setSettleAdminId(adminId);
        order.setSettleAdminName(adminUsername);
        order.setSettleRemark(request.getRemark());
        order.setSettledAt(LocalDateTime.now());

        betOrderMapper.updateById(order);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "结算投注订单",
                adminId,
                adminUsername,
                "订单号：" + order.getOrderNo()
                        + "，结算金额：" + settleAmount
                        + "，备注：" + request.getRemark()
        );
    }

    /**
     * 根据订单状态计算结算金额。
     *
     * @param order 投注订单
     * @return 结算金额
     */
    private BigDecimal calculateSettleAmount(
            BetOrderEntity order) {

        if (STATUS_WIN.equals(order.getStatus())) {
            return order.getTotalExpectedReturn();
        }

        if (STATUS_REFUNDED.equals(order.getStatus())) {
            return order.getTotalBetAmount();
        }

        throw new BusinessException(ResultCode.BET_ORDER_STATUS_NOT_ALLOW_SETTLE);
    }

    /**
     * 查询投注订单，不存在时抛出异常。
     *
     * @param orderId 订单 ID
     * @return 投注订单
     */
    private BetOrderEntity getOrderOrThrow(
            Long orderId) {

        if (orderId == null) {
            throw new BusinessException(ResultCode.BET_ORDER_ID_NOT_NULL);
        }

        BetOrderEntity order =
                betOrderMapper.selectById(orderId);

        if (order == null) {
            throw new BusinessException(ResultCode.BET_ORDER_NOT_FOUND);
        }

        return order;
    }

    /**
     * 校验备注不能为空。
     *
     * @param remark 备注
     */
    private void requireRemark(
            String remark) {

        if (remark == null || remark.isBlank()) {
            throw new BusinessException(ResultCode.BET_ORDER_REMARK_NOT_NULL);
        }
    }

    /**
     * 校验人工审核结果是否符合系统判定结果。
     *
     * @param systemResult 系统判定结果
     * @param reviewResult 人工审核结果
     */
    private void validateReviewResult(
            String systemResult,
            String reviewResult) {

        if (reviewResult == null || reviewResult.isBlank()) {
            throw new BusinessException(ResultCode.BET_ORDER_REVIEW_RESULT_NOT_NULL);
        }

        if (STATUS_FROZEN.equals(reviewResult)) {
            throw new BusinessException(ResultCode.BET_ORDER_FREEZE_USE_FREEZE_API);
        }

        if (systemResult == null || systemResult.isBlank()) {
            throw new BusinessException(ResultCode.BET_ORDER_SYSTEM_RESULT_NOT_EXISTS);
        }

        if (SYSTEM_WIN.equals(systemResult)) {
            if (!STATUS_WIN.equals(reviewResult)
                    && !STATUS_CANCELLED.equals(reviewResult)) {
                throw new BusinessException(ResultCode.BET_ORDER_SYSTEM_WIN_REVIEW_RESULT_INVALID);
            }
            return;
        }

        if (SYSTEM_LOSE.equals(systemResult)) {
            if (!STATUS_LOSE.equals(reviewResult)
                    && !STATUS_CANCELLED.equals(reviewResult)) {
                throw new BusinessException(ResultCode.BET_ORDER_SYSTEM_LOSE_REVIEW_RESULT_INVALID);
            }
            return;
        }

        if (SYSTEM_REFUNDED.equals(systemResult)) {
            if (!STATUS_REFUNDED.equals(reviewResult)
                    && !STATUS_CANCELLED.equals(reviewResult)) {
                throw new BusinessException(ResultCode.BET_ORDER_SYSTEM_REFUNDED_REVIEW_RESULT_INVALID);
            }
            return;
        }

        if (SYSTEM_CANCELLED.equals(systemResult)) {
            if (!STATUS_CANCELLED.equals(reviewResult)) {
                throw new BusinessException(ResultCode.BET_ORDER_SYSTEM_CANCELLED_REVIEW_RESULT_INVALID);
            }
            return;
        }

        throw new BusinessException(ResultCode.BET_ORDER_SYSTEM_RESULT_UNKNOWN);
    }
}
