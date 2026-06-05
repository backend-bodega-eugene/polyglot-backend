package com.eugene.goalhub.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.entity.BetOrderEntity;
import com.eugene.goalhub.order.entity.BetOrderItemEntity;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台投注订单管理服务实现。
 *
 * <p>负责后台投注订单分页查询、明细查询、人工审核、冻结和结算处理。</p>
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
     * 扣款中订单状态。
     */
    private static final String STATUS_DEDUCTING = "DEDUCTING";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认和最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

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

        if (request == null) {
            request = new AdminBetOrderPageRequest();
        }

        initPage(request);
        Integer pageIndex = request.getPageIndex();
        Integer pageSize = request.getPageSize();

        Page<AdminBetOrderResponse> page =
                new Page<>(pageIndex, pageSize);

        Page<AdminBetOrderResponse> resultPage =
                betOrderMapper.selectAdminOrderPage(
                        page,
                        request
                );

        goalhubLogService.sysLog(
                MODULE_NAME,
                "ORDER_PAGE",
                "分页查询投注订单，pageIndex=" + pageIndex
                        + ", pageSize=" + pageSize
                        + ", orderNo=" + request.getOrderNo()
                        + ", userId=" + request.getUserId()
                        + ", status=" + request.getStatus()
                        + ", total=" + resultPage.getTotal()
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

        if (request == null) {
            request = new AdminBetOrderItemPageRequest();
        }

        initPage(request);

        if (request.getLangCode() == null
                || request.getLangCode().isBlank()) {

            request.setLangCode("zh-CN");
        }

        Integer pageIndex = request.getPageIndex();
        Integer pageSize = request.getPageSize();

        Page<AdminBetOrderItemResponse> page =
                new Page<>(pageIndex, pageSize);

        Page<AdminBetOrderItemResponse> resultPage =
                betOrderItemMapper.selectAdminOrderItemPage(
                        page,
                        request
                );

        goalhubLogService.sysLog(
                MODULE_NAME,
                "ORDER_ITEM_PAGE",
                "分页查询投注订单明细，pageIndex=" + pageIndex
                        + ", pageSize=" + pageSize
                        + ", orderId=" + request.getOrderId()
                        + ", orderNo=" + request.getOrderNo()
                        + ", total=" + resultPage.getTotal()
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

        if (STATUS_SETTLED.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BET_ORDER_ALREADY_SETTLED);
        }

        validateReviewResult(request.getReviewResult());

        order.setStatus(request.getReviewResult());
        order.setReviewResult(request.getReviewResult());
        order.setReviewRemark(request.getRemark());
        order.setReviewAdminId(adminId);
        order.setReviewAdminName(adminUsername);
        order.setReviewedAt(LocalDateTime.now());

        int affectedRows = updateOrderOrThrow(order);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "审核投注订单",
                adminId,
                adminUsername,
                "订单号：" + order.getOrderNo()
                        + "，系统判定结果：" + order.getSystemResult()
                        + "，审核结果：" + request.getReviewResult()
                        + "，备注：" + request.getRemark()
                        + "，影响行数：" + affectedRows
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

        int affectedRows = updateOrderOrThrow(order);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "冻结投注订单",
                adminId,
                adminUsername,
                "订单号：" + order.getOrderNo()
                        + "，备注：" + request.getRemark()
                        + "，影响行数：" + affectedRows
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
            balanceRequest.setBizId("SETTLE_" + order.getOrderNo());
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

        int affectedRows = updateOrderOrThrow(order);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "结算投注订单",
                adminId,
                adminUsername,
                "订单号：" + order.getOrderNo()
                        + "，结算金额：" + settleAmount
                        + "，备注：" + request.getRemark()
                        + "，影响行数：" + affectedRows
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

        if (STATUS_LOSE.equals(order.getStatus())) {
            return BigDecimal.ZERO;
        }

        if (STATUS_WIN.equals(order.getStatus())) {
            return calculateWinSettleAmount(order.getId());
        }

        if (STATUS_REFUNDED.equals(order.getStatus())
                || STATUS_CANCELLED.equals(order.getStatus())) {
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
                betOrderMapper.selectByIdForUpdate(orderId);

        if (order == null) {
            throw new BusinessException(ResultCode.BET_ORDER_NOT_FOUND);
        }

        return order;
    }

    /**
     * 更新订单，不成功时抛出异常。
     *
     * @param order 投注订单
     * @return 影响行数
     */
    private int updateOrderOrThrow(
            BetOrderEntity order) {

        int affectedRows = betOrderMapper.updateById(order);

        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        return affectedRows;
    }

    /**
     * 初始化订单分页参数。
     *
     * @param request 分页请求
     */
    private void initPage(
            AdminBetOrderPageRequest request) {

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
     * 初始化订单明细分页参数。
     *
     * @param request 分页请求
     */
    private void initPage(
            AdminBetOrderItemPageRequest request) {

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
     * 根据订单明细快照计算赢单结算金额。
     *
     * @param orderId 订单 ID
     * @return 赢单结算金额
     */
    private BigDecimal calculateWinSettleAmount(
            Long orderId) {

        List<BetOrderItemEntity> items =
                betOrderItemMapper.selectList(
                        Wrappers.lambdaQuery(BetOrderItemEntity.class)
                                .eq(BetOrderItemEntity::getOrderId, orderId)
                );

        if (items == null || items.isEmpty()) {
            throw new BusinessException(ResultCode.BET_ORDER_NOT_FOUND);
        }

        BigDecimal settleAmount = BigDecimal.ZERO;

        for (BetOrderItemEntity item : items) {
            BigDecimal betAmount = item.getBetAmount();
            BigDecimal odds = item.getOdds();

            if (betAmount == null
                    || odds == null
                    || betAmount.compareTo(BigDecimal.ZERO) <= 0
                    || odds.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ResultCode.BET_ORDER_SETTLE_AMOUNT_INVALID);
            }

            BigDecimal profit = betAmount
                    .multiply(odds)
                    .subtract(betAmount)
                    .setScale(2, RoundingMode.DOWN);

            if (profit.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ResultCode.BET_ORDER_SETTLE_AMOUNT_INVALID);
            }

            settleAmount = settleAmount.add(profit);
        }

        return settleAmount.setScale(2, RoundingMode.DOWN);
    }

    /**
     * 校验人工审核结果是否合法。
     *
     * @param reviewResult 人工审核结果
     */
    private void validateReviewResult(
            String reviewResult) {

        if (reviewResult == null || reviewResult.isBlank()) {
            throw new BusinessException(ResultCode.BET_ORDER_REVIEW_RESULT_NOT_NULL);
        }

        if (STATUS_PENDING.equals(reviewResult)
                || STATUS_FROZEN.equals(reviewResult)
                || STATUS_LOSE.equals(reviewResult)
                || STATUS_WIN.equals(reviewResult)
                || STATUS_REFUNDED.equals(reviewResult)
                || STATUS_CANCELLED.equals(reviewResult)) {
            return;
        }

        throw new BusinessException(ResultCode.BET_ORDER_SYSTEM_RESULT_UNKNOWN);
    }
}
