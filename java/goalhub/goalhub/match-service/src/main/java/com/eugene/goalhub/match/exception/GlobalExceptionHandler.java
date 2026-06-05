package com.eugene.goalhub.match.exception;

import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import response.Result;
import response.ResultCode;

/**
 * match-service 全局异常处理器。
 *
 * <p>集中处理业务异常和未预期异常，并写入异步日志。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建 match-service 全局异常处理器。
     *
     * @param goalhubLogService 日志写入服务
     */
    public GlobalExceptionHandler(GoalhubLogService goalhubLogService) {
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 处理业务异常，保留业务错误码和错误信息。
     *
     * @param e 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        if (isFeignException(e)) {
            goalhubLogService.sysLog(
                    "match-service",
                    "FEIGN_CALL_FAILED",
                    "下游服务调用失败，code=" + e.getCode() + ", message=" + e.getMessage()
            );
        }

        goalhubLogService.errLog("match-service", "BUSINESS_EXCEPTION", e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理请求体参数校验异常。
     *
     * @param e 请求体参数校验异常
     * @return 统一参数错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ResultCode.PARAM_ERROR.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR, message);
    }

    /**
     * 处理请求参数校验异常。
     *
     * @param e 请求参数校验异常
     * @return 统一参数错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse(ResultCode.PARAM_ERROR.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR, message);
    }

    /**
     * 处理请求参数缺失异常。
     *
     * @param e 请求参数缺失异常
     * @return 统一参数错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return Result.fail(ResultCode.PARAM_ERROR);
    }

    /**
     * 处理请求体缺失或格式错误异常。
     *
     * @param e 请求体缺失或格式错误异常
     * @return 统一参数错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return Result.fail(ResultCode.PARAM_ERROR);
    }

    /**
     * 处理未预期异常。
     *
     * @param e 未预期异常
     * @return 统一内部错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        goalhubLogService.sysLog(
                "match-service",
                "UNEXPECTED_EXCEPTION",
                "match-service 发生未预期异常"
        );
        goalhubLogService.errLog("match-service", "UNEXPECTED_EXCEPTION", e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }

    /**
     * 判断是否为下游 Feign 调用异常。
     *
     * @param e 业务异常
     * @return 是下游 Feign 调用异常时返回 true
     */
    private boolean isFeignException(BusinessException e) {
        String message = e.getMessage();
        return message != null && message.contains("feign.");
    }
}
