package com.chokchok.gateway.exception.base;

import com.chokchok.gateway.exception.code.ErrorCode;
import lombok.Getter;

/**
 * UnauthorizedException 예외 클래스
 */
@Getter
public class UnauthorizedException extends RuntimeException {
    private final ErrorCode errorCode;

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
