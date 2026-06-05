package com.eugene.goalhub.order.service.impl;

import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.client.OrderUserAccountFeignClient;
import com.eugene.goalhub.order.service.OrderUserAccountService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.Result;
import response.ResultCode;

/**
 * 订单服务侧用户账户管理服务实现。
 *
 * <p>通过 user-service Feign 客户端执行账户余额增加和扣减，并统一校验远程调用结果。</p>
 */
@Service
public class OrderUserAccountServiceImpl
        implements OrderUserAccountService {

    /**
     * 系统日志模块名称。
     */
    private static final String MODULE_NAME = "订单用户账户";

    /**
     * 用户账户 Feign 客户端。
     */
    private final OrderUserAccountFeignClient
            orderUserAccountFeignClient;

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建订单服务侧用户账户管理服务实现。
     *
     * @param orderUserAccountFeignClient 用户账户 Feign 客户端
     * @param goalhubLogService           日志写入服务
     */
    public OrderUserAccountServiceImpl(
            OrderUserAccountFeignClient orderUserAccountFeignClient,
            GoalhubLogService goalhubLogService) {

        this.orderUserAccountFeignClient =
                orderUserAccountFeignClient;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 增加用户账户余额。
     *
     * @param request 账户余额增加参数
     */
    @Override
    public void addBalance(
            AdminAccountBalanceChangeRequest request) {

        checkSuccess(
                orderUserAccountFeignClient.addBalance(request)
        );
        goalhubLogService.sysLog(
                MODULE_NAME,
                "ADD_BALANCE",
                "调用用户账户加款成功，accountId=" + request.getAccountId()
                        + ", amount=" + request.getAmount()
        );
    }

    /**
     * 扣减用户账户余额。
     *
     * @param request 账户余额扣减参数
     */
    @Override
    public void subBalance(
            AdminAccountBalanceChangeRequest request) {

        checkSuccess(
                orderUserAccountFeignClient.subBalance(request)
        );
        goalhubLogService.sysLog(
                MODULE_NAME,
                "SUB_BALANCE",
                "调用用户账户扣款成功，accountId=" + request.getAccountId()
                        + ", amount=" + request.getAmount()
        );
    }

    /**
     * 校验远程账户服务返回结果。
     *
     * @param result 远程调用返回结果
     */
    private void checkSuccess(Result<?> result) {
        if (result == null) {
            throw new BusinessException(ResultCode.ORDER_USER_ACCOUNT_FEIGN_RESULT_NULL);
        }

        if (result.getCode() != ResultCode.SUCCESS.getCode()) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
    }
}
