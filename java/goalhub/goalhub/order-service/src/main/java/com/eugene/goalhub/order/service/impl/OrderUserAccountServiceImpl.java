package com.eugene.goalhub.order.service.impl;

import com.eugene.goalhub.order.client.OrderUserAccountFeignClient;
import com.eugene.goalhub.order.service.OrderUserAccountService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.Result;
import response.ResultCode;

/**
 * 订单服务侧用户账户管理服务实现。
 */
@Service
public class OrderUserAccountServiceImpl
        implements OrderUserAccountService {

    /**
     * 用户账户 Feign 客户端。
     */
    private final OrderUserAccountFeignClient
            orderUserAccountFeignClient;

    /**
     * 创建订单服务侧用户账户管理服务实现。
     *
     * @param orderUserAccountFeignClient 用户账户 Feign 客户端
     */
    public OrderUserAccountServiceImpl(
            OrderUserAccountFeignClient orderUserAccountFeignClient) {

        this.orderUserAccountFeignClient =
                orderUserAccountFeignClient;
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
            throw new BusinessException(ResultCode.ORDER_USER_ACCOUNT_FEIGN_RESULT_FAIL);
        }
    }
}
