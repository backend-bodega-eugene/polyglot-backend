package com.common.interfaces;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 响应状态码
 */
public interface ResultStatus extends Serializable {
    int OK = 0;
    int INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();
    int NOT_FOUND = HttpStatus.NOT_FOUND.value();
    int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    int UNAUTHORIZED = HttpStatus.UNAUTHORIZED.value();
    int FORBIDDEN = HttpStatus.FORBIDDEN.value();
}
