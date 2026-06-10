package com.eugene.goalhub.order.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * USDT 金额统一保留 4 位小数。
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

        BigDecimal betAmount = normalizeMoney(request.getAmount());

        if (betAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BET_AMOUNT_INVALID);
        }

        BigDecimal expectedReturn = betAmount
                .multiply(snapshot.getOdds())
                .setScale(MONEY_SCALE, RoundingMode.DOWN);

        BigDecimal expectedProfit = expectedReturn
                .subtract(betAmount)
                .setScale(MONEY_SCALE, RoundingMode.DOWN);

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
    @Override
    public PageResponse<AppBetOrderResponse> pageUnsettledOrders(
            Long userId,
            AppBetOrderPageRequest request) {

        return pageOrders(userId, request, false);
    }

    @Override
    public PageResponse<AppBetOrderResponse> pageSettledOrders(
            Long userId,
            AppBetOrderPageRequest request) {

        return pageOrders(userId, request, true);
    }
    @Override
    public PageResponse<AppBetOrderResponse> pageMyOrders(
            Long userId,
            AppBetOrderPageRequest request) {

        return pageOrders(userId, request, null);
    }
    private PageResponse<AppBetOrderResponse> pageOrders(
            Long userId,
            AppBetOrderPageRequest request,
            Boolean settled) {

        if (userId == null) {
            throw new BusinessException(ResultCode.USER_ID_NOT_NULL);
        }

        if (request == null) {
            request = new AppBetOrderPageRequest();
        }

        int pageIndex = request.getPageIndex() == null || request.getPageIndex() <= 0
                ? 1
                : request.getPageIndex();

        int pageSize = request.getPageSize() == null || request.getPageSize() <= 0
                ? 10
                : request.getPageSize();

        Page<AppBetOrderResponse> page = new Page<>(pageIndex, pageSize);

        Page<AppBetOrderResponse> resultPage;

        if (settled == null) {
            resultPage = betOrderMapper.selectAppMyOrderPage(page, userId, request);
        } else {
            resultPage = betOrderMapper.selectAppOrderPage(page, userId, settled);
        }

        List<AppBetOrderResponse> orders = resultPage.getRecords();

        if (orders == null || orders.isEmpty()) {
            return  new PageResponse<>(
                    resultPage.getTotal(),
                    Math.toIntExact(resultPage.getCurrent()),
                    Math.toIntExact(resultPage.getSize()),
                    orders
            );
        }

        List<Long> orderIds = orders.stream()
                .map(AppBetOrderResponse::getOrderId)
                .toList();

        String langCode = request.getLangCode();
        if (langCode == null || langCode.isBlank()) {
            langCode = "zh-CN";
        }

        List<AppBetOrderItemResponse> items =
                betOrderItemMapper.selectAppOrderItemsByOrderIds(
                        orderIds,
                        langCode
                );

        Map<Long, List<AppBetOrderItemResponse>> itemMap =
                items.stream()
                        .collect(Collectors.groupingBy(
                                AppBetOrderItemResponse::getOrderId
                        ));

        for (AppBetOrderResponse order : orders) {
            normalizeOrderResponse(order);

            List<AppBetOrderItemResponse> orderItems =
                    itemMap.getOrDefault(order.getOrderId(), List.of());

            orderItems.forEach(this::normalizeOrderItemResponse);
            order.setItems(orderItems);
        }

        return  new PageResponse<>(
                resultPage.getCurrent(),
                Math.toIntExact(resultPage.getSize()),
                Math.toIntExact(resultPage.getTotal()),
                orders
        );
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
    }

    /**
     * 规范化 USDT 金额，小数位不足时补 0，超过时由上游校验拦截。
     *
     * @param amount 原始金额
     * @return 4 位小数金额
     */
    private BigDecimal normalizeMoney(
            BigDecimal amount) {

        return amount.setScale(MONEY_SCALE, RoundingMode.DOWN);
    }

    private void normalizeOrderResponse(
            AppBetOrderResponse response) {

        response.setTotalBetAmount(normalizeNullableMoney(response.getTotalBetAmount()));
        response.setTotalExpectedProfit(normalizeNullableMoney(response.getTotalExpectedProfit()));
        response.setTotalExpectedReturn(normalizeNullableMoney(response.getTotalExpectedReturn()));
        response.setSettleAmount(normalizeNullableMoney(response.getSettleAmount()));
    }

    private void normalizeOrderItemResponse(
            AppBetOrderItemResponse response) {

        response.setBetAmount(normalizeNullableMoney(response.getBetAmount()));
        response.setExpectedProfit(normalizeNullableMoney(response.getExpectedProfit()));
        response.setExpectedReturn(normalizeNullableMoney(response.getExpectedReturn()));
    }

    private BigDecimal normalizeNullableMoney(
            BigDecimal amount) {

        if (amount == null) {
            return null;
        }

        return normalizeMoney(amount);
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
