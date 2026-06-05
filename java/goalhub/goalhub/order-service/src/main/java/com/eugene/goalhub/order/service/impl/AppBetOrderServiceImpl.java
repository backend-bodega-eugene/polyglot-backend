package com.eugene.goalhub.order.service.impl;

import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.client.OrderMatchFeignClient;
import com.eugene.goalhub.order.client.OrderUserAccountFeignClient;
import com.eugene.goalhub.order.entity.BetOrderEntity;
import com.eugene.goalhub.order.entity.BetOrderItemEntity;
import com.eugene.goalhub.order.mapper.BetOrderItemMapper;
import com.eugene.goalhub.order.mapper.BetOrderMapper;
import com.eugene.goalhub.order.service.AppBetOrderService;
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
 * 前端投注订单服务实现。
 *
 * <p>负责校验下单参数、读取赛事赔率快照、扣减用户默认账户余额，并生成投注订单和订单明细。</p>
 */
@Service
public class AppBetOrderServiceImpl
        implements AppBetOrderService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "前端投注订单";

    /**
     * 待判定订单状态。
     */
    private static final String STATUS_PENDING = "PENDING";

    /**
     * 扣款中订单状态。
     */
    private static final String STATUS_DEDUCTING = "DEDUCTING";

    /**
     * 可投注状态。
     */
    private static final String BET_STATUS_OPEN = "OPEN";

    /**
     * 未开始比赛状态。
     */
    private static final String MATCH_STATUS_NOT_STARTED = "NOT_STARTED";

    /**
     * 进行中比赛状态。
     */
    private static final String MATCH_STATUS_LIVE = "LIVE";

    /**
     * 下单扣款业务备注前缀。
     */
    private static final String BIZ_REMARK_PLACE_BET = "用户下注扣款";

    /**
     * 投注订单 Mapper。
     */
    private final BetOrderMapper betOrderMapper;

    /**
     * 投注订单明细 Mapper。
     */
    private final BetOrderItemMapper betOrderItemMapper;

    /**
     * 赛事快照 Feign 客户端。
     */
    private final OrderMatchFeignClient orderMatchFeignClient;

    /**
     * 用户账户 Feign 客户端。
     */
    private final OrderUserAccountFeignClient orderUserAccountFeignClient;

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建前端投注订单服务实现。
     *
     * @param betOrderMapper                投注订单 Mapper
     * @param betOrderItemMapper            投注订单明细 Mapper
     * @param orderMatchFeignClient         赛事快照 Feign 客户端
     * @param orderUserAccountFeignClient 用户账户 Feign 客户端
     * @param goalhubLogService           日志写入服务
     */
    public AppBetOrderServiceImpl(
            BetOrderMapper betOrderMapper,
            BetOrderItemMapper betOrderItemMapper,
            OrderMatchFeignClient orderMatchFeignClient,
            OrderUserAccountFeignClient orderUserAccountFeignClient,
            GoalhubLogService goalhubLogService) {
        this.betOrderMapper = betOrderMapper;
        this.betOrderItemMapper = betOrderItemMapper;
        this.orderMatchFeignClient = orderMatchFeignClient;
        this.orderUserAccountFeignClient = orderUserAccountFeignClient;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 提交投注订单。
     *
     * <p>下单流程包括参数校验、赛事赔率快照校验、默认账户扣款、订单主表写入和订单明细写入。</p>
     *
     * @param userId  当前登录用户 ID
     * @param request 投注下单参数
     * @return 投注下单结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlaceBetOrderResponse placeOrder(
            Long userId,
            PlaceBetOrderRequest request) {

        checkRequest(userId, request);

        OrderMatchOptionSnapshotResponse snapshot =
                getMatchOptionSnapshot(request.getMatchMarketOptionId());

        checkSnapshot(snapshot);

        BigDecimal betAmount = request.getAmount()
                .setScale(2, RoundingMode.DOWN);

        BigDecimal expectedReturn = betAmount
                .multiply(snapshot.getOdds())
                .setScale(2, RoundingMode.DOWN);

        BigDecimal expectedProfit = expectedReturn
                .subtract(betAmount)
                .setScale(2, RoundingMode.DOWN);

        String orderNo = generateOrderNo();

        BetOrderEntity order = new BetOrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalBetAmount(betAmount);
        order.setTotalExpectedProfit(expectedProfit);
        order.setTotalExpectedReturn(expectedReturn);
        order.setStatus(STATUS_DEDUCTING);

        betOrderMapper.insert(order);

        BetOrderItemEntity item = new BetOrderItemEntity();
        item.setOrderId(order.getId());
        item.setOrderNo(orderNo);
        item.setMatchId(snapshot.getMatchId());
        item.setPlayId(snapshot.getMarketId());
        item.setOptionId(snapshot.getMarketOptionId());
        item.setPlayCode(snapshot.getMarketCode());
        item.setPlayName(snapshot.getMarketName());
        item.setOptionCode(snapshot.getMarketOptionCode());
        item.setOptionName(snapshot.getMarketOptionName());
        item.setOdds(snapshot.getOdds());
        item.setBetAmount(betAmount);
        item.setExpectedProfit(expectedProfit);
        item.setExpectedReturn(expectedReturn);

        betOrderItemMapper.insert(item);

        DeductDefaultAccountResponse accountResponse =
                deductDefaultUsdt(
                        userId,
                        betAmount,
                        orderNo
                );

        order.setAccountId(accountResponse.getAccountId());
        order.setCurrencyCode(accountResponse.getCurrencyCode());
        order.setBalanceBefore(accountResponse.getBalanceBefore());
        order.setBalanceAfter(accountResponse.getBalanceAfter());
        order.setStatus(STATUS_PENDING);
        updateOrderOrThrow(order);

        goalhubLogService.bizLog(
                MODULE_NAME,
                "PLACE_BET_ORDER",
                userId,
                null,
                "用户提交投注订单成功，orderId=" + order.getId()
                        + ", orderNo=" + order.getOrderNo()
                        + ", matchId=" + snapshot.getMatchId()
                        + ", matchMarketOptionId=" + request.getMatchMarketOptionId()
                        + ", amount=" + betAmount
        );

        PlaceBetOrderResponse response = new PlaceBetOrderResponse();
        response.setOrderId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setStatus(order.getStatus());
        response.setBetAmount(betAmount);
        response.setOdds(snapshot.getOdds());
        response.setExpectedProfit(expectedProfit);
        response.setExpectedReturn(expectedReturn);
        response.setBalanceAfter(accountResponse.getBalanceAfter());

        return response;
    }

    /**
     * 校验投注下单请求。
     *
     * @param userId  当前登录用户 ID
     * @param request 投注下单参数
     */
    private void checkRequest(
            Long userId,
            PlaceBetOrderRequest request) {

        if (userId == null) {
            throw new BusinessException(ResultCode.USER_ID_NOT_NULL);
        }

        if (request == null) {
            throw new BusinessException(ResultCode.BET_ORDER_REQUEST_NOT_NULL);
        }

        if (request.getMatchMarketOptionId() == null) {
            throw new BusinessException(ResultCode.MATCH_MARKET_OPTION_ID_NOT_NULL);
        }

        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BET_AMOUNT_INVALID);
        }

        if (request.getAmount().stripTrailingZeros().scale() > 2) {
            throw new BusinessException(ResultCode.BET_AMOUNT_INVALID);
        }
    }

    /**
     * 查询赛事玩法选项快照。
     *
     * @param matchMarketOptionId 赛事玩法选项 ID
     * @return 赛事玩法选项快照
     */
    private OrderMatchOptionSnapshotResponse getMatchOptionSnapshot(
            Long matchMarketOptionId) {

        Result<OrderMatchOptionSnapshotResponse> result =
                orderMatchFeignClient.getMatchOptionSnapshot(
                        matchMarketOptionId
                );

        if (result == null) {
            throw new BusinessException(ResultCode.ORDER_MATCH_FEIGN_RESULT_NULL);
        }

        if (result.getCode() != ResultCode.SUCCESS.getCode()) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }

        if (result.getData() == null) {
            throw new BusinessException(ResultCode.ORDER_MATCH_SNAPSHOT_NOT_FOUND);
        }

        goalhubLogService.sysLog(
                MODULE_NAME,
                "GET_MATCH_OPTION_SNAPSHOT",
                "查询赛事玩法选项快照成功，matchMarketOptionId=" + matchMarketOptionId
        );
        return result.getData();
    }

    /**
     * 校验赛事玩法选项快照是否允许投注。
     *
     * @param snapshot 赛事玩法选项快照
     */
    private void checkSnapshot(
            OrderMatchOptionSnapshotResponse snapshot) {

        if (!Integer.valueOf(1).equals(snapshot.getVisible())) {
            throw new BusinessException(ResultCode.BET_OPTION_NOT_VISIBLE);
        }

        if (!BET_STATUS_OPEN.equals(snapshot.getBetStatus())) {
            throw new BusinessException(ResultCode.BET_OPTION_NOT_OPEN);
        }

        if (snapshot.getOdds() == null
                || snapshot.getOdds().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BET_ODDS_INVALID);
        }

        if (!MATCH_STATUS_NOT_STARTED.equals(snapshot.getMatchStatus())
                && !MATCH_STATUS_LIVE.equals(snapshot.getMatchStatus())) {
            throw new BusinessException(ResultCode.MATCH_NOT_ALLOW_BET);
        }
    }

    /**
     * 扣减用户默认 USDT 账户余额。
     *
     * @param userId  用户 ID
     * @param amount  扣款金额
     * @param orderNo 订单号
     * @return 默认账户扣款结果
     */
    private DeductDefaultAccountResponse deductDefaultUsdt(
            Long userId,
            BigDecimal amount,
            String orderNo) {

        DeductDefaultAccountRequest request =
                new DeductDefaultAccountRequest();

        request.setUserId(userId);
        request.setAmount(amount);
        request.setBizId(orderNo);
        request.setRemark(BIZ_REMARK_PLACE_BET + "，订单号：" + orderNo);

        Result<DeductDefaultAccountResponse> result =
                orderUserAccountFeignClient.deductDefaultUsdt(request);

        if (result == null) {
            throw new BusinessException(ResultCode.ORDER_ACCOUNT_FEIGN_RESULT_NULL);
        }

        if (result.getCode() != ResultCode.SUCCESS.getCode()) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }

        if (result.getData() == null) {
            throw new BusinessException(ResultCode.ORDER_ACCOUNT_DEDUCT_RESULT_NULL);
        }

        goalhubLogService.sysLog(
                MODULE_NAME,
                "DEDUCT_DEFAULT_USDT",
                "扣减用户默认 USDT 账户成功，userId=" + userId
                        + ", orderNo=" + orderNo
                        + ", amount=" + amount
        );
        return result.getData();
    }

    /**
     * 生成投注订单号。
     *
     * @return 投注订单号
     */
    private String generateOrderNo() {

        return "BO"
                + UUID.randomUUID()
                .toString()
                .replace("-", "");
    }

    /**
     * 更新订单，不成功时抛出异常。
     *
     * @param order 投注订单
     */
    private void updateOrderOrThrow(
            BetOrderEntity order) {

        int affectedRows = betOrderMapper.updateById(order);

        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
