package com.eugene.goalhub.user.exception;

import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import response.Result;
import response.ResultCode;

import java.util.stream.Collectors;

/**
 * user-service 全局异常处理器。
 *
 * <p>统一将业务异常和未预期异常转换为项目标准响应结构。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建 user-service 全局异常处理器。
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
        goalhubLogService.errLog("user-service", "BUSINESS_EXCEPTION", e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理请求体参数校验异常。
     *
     * @param e 参数校验异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理请求参数约束异常。
     *
     * @param e 参数约束异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(
            ConstraintViolationException e) {

        String message = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理缺少请求参数异常。
     *
     * @param e 缺少请求参数异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {

        return Result.fail(
                ResultCode.PARAM_ERROR.getCode(),
                e.getParameterName() + "不能为空"
        );
    }

    /**
     * 处理缺少请求头异常。
     *
     * @param e 缺少请求头异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Void> handleMissingRequestHeaderException(
            MissingRequestHeaderException e) {

        return Result.fail(
                ResultCode.PARAM_ERROR.getCode(),
                e.getHeaderName() + "不能为空"
        );
    }

    /**
     * 处理请求体不可读异常。
     *
     * @param e 请求体不可读异常
     * @return 统一失败响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {

        return Result.fail(
                ResultCode.PARAM_ERROR.getCode(),
                "请求体不能为空或格式错误"
        );
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
                "user-service",
                "UNEXPECTED_EXCEPTION",
                "user-service 发生未预期异常"
        );
        goalhubLogService.errLog("user-service", "UNEXPECTED_EXCEPTION", e);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
