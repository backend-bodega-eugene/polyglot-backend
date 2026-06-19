package com.eugene.goalhub.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.entity.BetOrderEntity;
import com.eugene.goalhub.order.entity.BetOrderItemEntity;
import com.eugene.goalhub.order.judge.BetResultJudgeRouter;
import com.eugene.goalhub.order.judge.BetSystemResult;
import com.eugene.goalhub.order.mapper.BetOrderItemMapper;
import com.eugene.goalhub.order.mapper.BetOrderMapper;
import com.eugene.goalhub.order.service.AdminOrderResultService;
import dto.AdminMatchResultJudgeRequest;
import dto.SaveMatchResultRequest;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台订单系统预判服务实现。
 *
 * <p>
 * 只负责根据赛事赛果写入订单系统预判结果和预期金额。
 * 不派奖，不扣钱，不写账变，不修改结算字段。
 * </p>
 */
@Service
public class AdminOrderResultServiceImpl
        implements AdminOrderResultService {

    /**
     * 日志模块名称。
     */
    private static final String MODULE_NAME = "订单系统预判";

    /**
     * 已结算订单状态。
     */
    private static final String STATUS_SETTLED = "SETTLED";

    /**
     * 系统判赢结果。
     */
    private static final String RESULT_WIN = "WIN";

    /**
     * 系统判输结果。
     */
    private static final String RESULT_LOSE = "LOSE";

    /**
     * 系统走水结果。
     */
    private static final String RESULT_PUSH = "PUSH";

    /**
     * 金额小数位。
     */
    private static final int MONEY_SCALE = 4;

    /**
     * 投注订单 Mapper。
     */
    private final BetOrderMapper betOrderMapper;

    /**
     * 投注订单明细 Mapper。
     */
    private final BetOrderItemMapper betOrderItemMapper;

    /**
     * 投注结果系统预判路由器。
     */
    private final BetResultJudgeRouter betResultJudgeRouter;

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建后台订单系统预判服务实现。
     *
     * @param betOrderMapper       投注订单 Mapper
     * @param betOrderItemMapper   投注订单明细 Mapper
     * @param betResultJudgeRouter 投注结果系统预判路由器
     * @param goalhubLogService    日志写入服务
     */
    public AdminOrderResultServiceImpl(
            BetOrderMapper betOrderMapper,
            BetOrderItemMapper betOrderItemMapper,
            BetResultJudgeRouter betResultJudgeRouter,
            GoalhubLogService goalhubLogService) {

        this.betOrderMapper = betOrderMapper;
        this.betOrderItemMapper = betOrderItemMapper;
        this.betResultJudgeRouter = betResultJudgeRouter;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 根据赛事赛果生成订单系统预判结果。
     *
     * @param request 赛事订单系统预判请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void judgeMatch(
            AdminMatchResultJudgeRequest request) {

        requireRequest(request);

        SaveMatchResultRequest matchResult =
                request.getMatchResult();

        requireMatchResult(matchResult);

        List<BetOrderItemEntity> matchItems =
                betOrderItemMapper.selectList(
                        Wrappers.lambdaQuery(BetOrderItemEntity.class)
                                .eq(BetOrderItemEntity::getMatchId, request.getMatchId())
                );

        if (matchItems == null || matchItems.isEmpty()) {
            goalhubLogService.bizLog(
                    MODULE_NAME,
                    "JUDGE_MATCH_ORDER_RESULT",
                    request.getAdminId(),
                    request.getAdminUsername(),
                    "赛事无投注订单明细，matchId=" + request.getMatchId()
            );
            return;
        }

        Set<Long> orderIds = matchItems.stream()
                .map(BetOrderItemEntity::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (orderIds.isEmpty()) {
            return;
        }

        Map<Long, BetOrderEntity> orderMap =
                betOrderMapper.selectBatchIds(orderIds)
                        .stream()
                        .collect(Collectors.toMap(
                                BetOrderEntity::getId,
                                order -> order
                        ));

        String resultSnapshot =
                buildMatchResultSnapshot(matchResult);

        int itemCount = 0;
        int orderCount = 0;

        for (Long orderId : orderIds) {

            BetOrderEntity order =
                    orderMap.get(orderId);

            if (order == null) {
                continue;
            }

            if (STATUS_SETTLED.equals(order.getStatus())) {
                continue;
            }

            List<BetOrderItemEntity> allOrderItems =
                    betOrderItemMapper.selectList(
                            Wrappers.lambdaQuery(BetOrderItemEntity.class)
                                    .eq(BetOrderItemEntity::getOrderId, orderId)
                    );

            if (allOrderItems == null || allOrderItems.isEmpty()) {
                continue;
            }

            for (BetOrderItemEntity item : allOrderItems) {

                if (!Objects.equals(item.getMatchId(), request.getMatchId())) {
                    continue;
                }

                BetSystemResult systemResult =
                        betResultJudgeRouter.judge(item, matchResult);

                applyItemSystemResult(
                        item,
                        systemResult,
                        resultSnapshot
                );

                int affectedRows =
                        betOrderItemMapper.updateById(item);

                if (affectedRows != 1) {
                    throw new BusinessException(ResultCode.PARAM_ERROR);
                }

                itemCount++;
            }

            List<BetOrderItemEntity> refreshedItems =
                    betOrderItemMapper.selectList(
                            Wrappers.lambdaQuery(BetOrderItemEntity.class)
                                    .eq(BetOrderItemEntity::getOrderId, orderId)
                    );

            applyOrderSystemResult(
                    order,
                    refreshedItems
            );

            int affectedRows =
                    betOrderMapper.updateById(order);

            if (affectedRows != 1) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }

            orderCount++;
        }

        goalhubLogService.bizLog(
                MODULE_NAME,
                "JUDGE_MATCH_ORDER_RESULT",
                request.getAdminId(),
                request.getAdminUsername(),
                "根据赛果生成订单系统预判结果成功，matchId=" + request.getMatchId()
                        + "，影响订单明细数=" + itemCount
                        + "，影响订单数=" + orderCount
                        + "，备注=" + request.getRemark()
        );
    }

    /**
     * 写入订单明细系统预判结果和预期金额。
     *
     * @param item           投注订单明细
     * @param systemResult   系统预判结果
     * @param resultSnapshot 比赛结果快照
     */
    private void applyItemSystemResult(
            BetOrderItemEntity item,
            BetSystemResult systemResult,
            String resultSnapshot) {

        item.setSystemResult(systemResult.name());
        item.setMatchResultSnapshot(resultSnapshot);

        if (BetSystemResult.WIN.equals(systemResult)) {
            BigDecimal expectedReturn =
                    calculateWinReturn(item);

            item.setExpectedReturn(expectedReturn);
            item.setExpectedProfit(
                    normalizeMoney(expectedReturn.subtract(requireMoney(item.getBetAmount())))
            );
            return;
        }

        if (BetSystemResult.LOSE.equals(systemResult)) {
            item.setExpectedReturn(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN));
            item.setExpectedProfit(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN));
            return;
        }

        if (BetSystemResult.PUSH.equals(systemResult)
                || BetSystemResult.CANCELLED.equals(systemResult)) {

            item.setExpectedReturn(
                    normalizeMoney(requireMoney(item.getBetAmount()))
            );
            item.setExpectedProfit(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN));
            return;
        }

        throw new BusinessException(ResultCode.BET_ORDER_SYSTEM_RESULT_UNKNOWN);
    }

    /**
     * 汇总订单明细系统结果并写入订单主表预判结果。
     *
     * @param order 投注订单
     * @param items 订单明细列表
     */
    private void applyOrderSystemResult(
            BetOrderEntity order,
            List<BetOrderItemEntity> items) {

        if (items == null || items.isEmpty()) {
            return;
        }

        boolean hasLose = items.stream()
                .anyMatch(item -> RESULT_LOSE.equals(item.getSystemResult()));

        if (hasLose) {
            order.setSystemResult(RESULT_LOSE);
            order.setTotalExpectedProfit(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN));
            order.setTotalExpectedReturn(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN));
            return;
        }

        boolean allJudged = items.stream()
                .allMatch(item -> item.getSystemResult() != null
                        && !item.getSystemResult().isBlank());

        if (!allJudged) {
            order.setSystemResult(null);
            order.setTotalExpectedProfit(sumExpectedProfit(items));
            order.setTotalExpectedReturn(sumExpectedReturn(items));
            return;
        }

        boolean hasPush = items.stream()
                .anyMatch(item -> RESULT_PUSH.equals(item.getSystemResult()));

        if (hasPush) {
            order.setSystemResult(RESULT_PUSH);
            order.setTotalExpectedProfit(sumExpectedProfit(items));
            order.setTotalExpectedReturn(sumExpectedReturn(items));
            return;
        }

        order.setSystemResult(RESULT_WIN);
        order.setTotalExpectedProfit(sumExpectedProfit(items));
        order.setTotalExpectedReturn(sumExpectedReturn(items));
    }

    /**
     * 计算判赢订单明细的预计返还金额。
     *
     * @param item 投注订单明细
     * @return 预计返还金额
     */
    private BigDecimal calculateWinReturn(
            BetOrderItemEntity item) {

        BigDecimal betAmount =
                requireMoney(item.getBetAmount());

        BigDecimal odds =
                requireMoney(item.getOdds());

        return normalizeMoney(
                betAmount.multiply(odds)
        );
    }

    /**
     * 汇总订单明细预计盈利金额。
     *
     * @param items 订单明细列表
     * @return 预计盈利总额
     */
    private BigDecimal sumExpectedProfit(
            List<BetOrderItemEntity> items) {

        BigDecimal total = BigDecimal.ZERO;

        for (BetOrderItemEntity item : items) {
            if (item.getExpectedProfit() != null) {
                total = total.add(item.getExpectedProfit());
            }
        }

        return normalizeMoney(total);
    }

    /**
     * 汇总订单明细预计返还金额。
     *
     * @param items 订单明细列表
     * @return 预计返还总额
     */
    private BigDecimal sumExpectedReturn(
            List<BetOrderItemEntity> items) {

        BigDecimal total = BigDecimal.ZERO;

        for (BetOrderItemEntity item : items) {
            if (item.getExpectedReturn() != null) {
                total = total.add(item.getExpectedReturn());
            }
        }

        return normalizeMoney(total);
    }

    /**
     * 构建比赛结果快照。
     *
     * @param result 比赛结果
     * @return 比分快照
     */
    private String buildMatchResultSnapshot(
            SaveMatchResultRequest result) {

        return result.getRegularHomeScore()
                + ":"
                + result.getRegularAwayScore();
    }

    /**
     * 校验订单系统预判请求。
     *
     * @param request 赛事订单系统预判请求
     */
    private void requireRequest(
            AdminMatchResultJudgeRequest request) {

        if (request == null || request.getMatchId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (request.getMatchResult() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (!Objects.equals(request.getMatchId(), request.getMatchResult().getMatchId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验赛事赛果具备预判所需比分。
     *
     * @param result 比赛结果
     */
    private void requireMatchResult(
            SaveMatchResultRequest result) {

        if (result.getRegularHomeScore() == null
                || result.getRegularAwayScore() == null) {

            throw new BusinessException(ResultCode.SCORE_CANT_NOT_NULL);
        }
    }

    /**
     * 校验金额字段有效。
     *
     * @param amount 金额
     * @return 原始金额
     */
    private BigDecimal requireMoney(
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.BET_ORDER_SETTLE_AMOUNT_INVALID);
        }

        return amount;
    }

    /**
     * 规范化金额精度。
     *
     * @param amount 金额
     * @return 统一精度后的金额
     */
    private BigDecimal normalizeMoney(
            BigDecimal amount) {

        return amount.setScale(MONEY_SCALE, RoundingMode.DOWN);
    }
}
