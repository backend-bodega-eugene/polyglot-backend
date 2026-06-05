package com.eugene.goalhub.admin.service.support;

import exception.BusinessException;
import response.Result;
import response.ResultCode;

/**
 * Feign 调用结果处理工具。
 *
 * <p>统一校验内部服务调用返回值，避免各业务服务重复判断 Feign 响应状态。</p>
 */
public final class FeignResultSupport {

    /**
     * 工具类不允许实例化。
     */
    private FeignResultSupport() {
    }

    /**
     * 校验 Feign 返回结果并提取数据。
     *
     * <p>当 Feign 响应为空或业务状态码非成功时，会抛出统一业务异常。</p>
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
     * <p>该方法只关心调用结果是否成功，不读取响应数据。</p>
     *
     * @param result Feign 返回结果
     */
    public static void checkSuccess(Result<?> result) {
        if (result == null) {
            throw new BusinessException(ResultCode.FEIGN_RESULT_NULL);
        }

        if (result.getCode() != ResultCode.SUCCESS.getCode()) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
    }
}
