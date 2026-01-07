package com.common.result;

public enum ResultEnum {
    UNKNOWN_ERROR(-1, "未知错误"),
    SUCCESS(0, "成功"),
    SERVER_INTERNAL_ERROR(500, "服务器内部错误"),
    RESOURCE_NOT_FOUND(404, "资源未找到"),
    PARAMETER_NOT_VALID(400, "参数不合法"),
    ;
    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}