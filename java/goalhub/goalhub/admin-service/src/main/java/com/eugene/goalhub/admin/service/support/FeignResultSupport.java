package com.eugene.goalhub.admin.service.support;

import exception.BusinessException;
import response.Result;
import response.ResultCode;

/**
 * Feign 调用结果处理工具。
 */
public final class FeignResultSupport {

    private FeignResultSupport() {
    }

    /**
     * 校验 Feign 返回结果并提取数据。
     *
     * @param result Feign 返回结果
     * @param <T>    数据类型
     * @return 响应数据
     */
    public static <T> T data(Result<T> result) {
        checkSuccess(result);
        return result.getData();
    }

    /**
     * 校验 Feign 返回结果是否成功。
     *
     * @param result Feign 返回结果
     */
    public static void checkSuccess(Result<?> result) {
        if (result == null) {
            throw new BusinessException(ResultCode.FAIL);
        }

        if (result.getCode() != ResultCode.SUCCESS.getCode()) {
            throw new BusinessException(ResultCode.FEIGN_RESULT_FAIL);
        }
    }
}
