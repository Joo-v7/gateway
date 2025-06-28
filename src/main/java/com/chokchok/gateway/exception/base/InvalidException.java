package com.chokchok.gateway.exception.base;

import com.chokchok.gateway.exception.code.ErrorCode;
import lombok.Getter;

/**
 * 유효하지 않은 요청에 대한 예외 처리 클래스
 */
@Getter
public class InvalidException extends RuntimeException {
    private final ErrorCode errorCode;

    public InvalidException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

