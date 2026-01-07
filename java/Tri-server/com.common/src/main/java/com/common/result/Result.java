package com.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 普通返回对象
 *
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {
    private final static int SUCCESS_CODE = 0;
    private final static String SUCCESS_TEXT = "success";
    @Schema(description = "任务状态 0是成功,其他都是失败")
    private Integer code;
    @Schema(description = "任务提示,根据状态提供,状态是普遍的成功 提示success")
    private String message;
    @Schema(description = "业务实体对象")
    private T data;

    public Result() {
        this.code = SUCCESS_CODE;
        this.message = SUCCESS_TEXT;
        this.data = null;
    }

    public Result(T data) {
        this.code = SUCCESS_CODE;
        this.message = SUCCESS_TEXT;
        this.data = data;
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

}
