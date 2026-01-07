package com.common.exception;

public class BizException extends ApplicationException {
    private Integer code;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }


    public BizException(Throwable cause) {
        super(cause);
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
