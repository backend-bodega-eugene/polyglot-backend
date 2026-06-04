package com.eugene.goalhub.order.service.impl;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 前端投注订单服务实现。
 */
@Service
public class AppBetOrderServiceImpl
        implements AppBetOrderService {

    private static final String STATUS_PENDING = "PENDING";

    private static final String BET_STATUS_OPEN = "OPEN";

    private static final String MATCH_STATUS_NOT_STARTED = "NOT_STARTED";

    private static final String MATCH_STATUS_LIVE = "LIVE";

    private static final String BIZ_REMARK_PLACE_BET = "用户下注扣款";

    private static final DateTimeFormatter ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final BetOrderMapper betOrderMapper;

    private final BetOrderItemMapper betOrderItemMapper;

    private final OrderMatchFeignClient orderMatchFeignClient;

    private final OrderUserAccountFeignClient orderUserAccountFeignClient;

    public AppBetOrderServiceImpl(
            BetOrderMapper betOrderMapper,
            BetOrderItemMapper betOrderItemMapper,
            OrderMatchFeignClient orderMatchFeignClient,
            OrderUserAccountFeignClient orderUserAccountFeignClient) {
        this.betOrderMapper = betOrderMapper;
        this.betOrderItemMapper = betOrderItemMapper;
        this.orderMatchFeignClient = orderMatchFeignClient;
        this.orderUserAccountFeignClient = orderUserAccountFeignClient;
    }

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

        String orderNo = generateOrderNo(
                userId,
                snapshot.getMatchId(),
                snapshot.getMarketOptionId()
        );

        DeductDefaultAccountResponse accountResponse =
                deductDefaultUsdt(
                        userId,
                        betAmount,
                        orderNo
                );

        BetOrderEntity order = new BetOrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAccountId(accountResponse.getAccountId());
        order.setTotalBetAmount(betAmount);
        order.setTotalExpectedProfit(expectedProfit);
        order.setTotalExpectedReturn(expectedReturn);
        order.setCurrencyCode(accountResponse.getCurrencyCode());
        order.setBalanceBefore(accountResponse.getBalanceBefore());
        order.setBalanceAfter(accountResponse.getBalanceAfter());
        order.setStatus(STATUS_PENDING);

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
    }

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
            throw new BusinessException(ResultCode.ORDER_MATCH_FEIGN_RESULT_FAIL);
        }

        if (result.getData() == null) {
            throw new BusinessException(ResultCode.ORDER_MATCH_SNAPSHOT_NOT_FOUND);
        }

        return result.getData();
    }

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
            throw new BusinessException(ResultCode.ORDER_ACCOUNT_FEIGN_RESULT_FAIL);
        }

        if (result.getData() == null) {
            throw new BusinessException(ResultCode.ORDER_ACCOUNT_DEDUCT_RESULT_NULL);
        }

        return result.getData();
    }

    private String generateOrderNo(
            Long userId,
            Long matchId,
            Long marketOptionId) {

        return userId
                + "_"
                + matchId
                + "_"
                + marketOptionId
                + "_"
                + LocalDateTime.now().format(ORDER_TIME_FORMATTER);
    }
}