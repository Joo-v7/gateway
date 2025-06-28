package com.chokchok.gateway.exception.dto;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO 클래스
 * error 발생 시 해당 DTO로 응답합니다.
 * chokchok-API 서버와의 통신 시 에러 응답을 받는데 사용합니다.
 *
 * @param error           에러 발생 여부 (true/false)
 * @param httpStatusCode  HTTP 상태 코드 (예: 400, 404 등)
 * @param errorCode       사용자 지정 에러 코드 (ErrorCode에서 정의된 값)
 * @param message         에러 메시지
 * @param timestamp       에러 발생 시간
 */
public record ErrorResponseDto(
        boolean error,
        int httpStatusCode,
        int errorCode,
        String message,
        LocalDateTime timestamp
) {

    public static ErrorResponseDto of(int httpStatusCode, int errorCode, String message) {
        return new ErrorResponseDto(true, httpStatusCode, errorCode, message, LocalDateTime.now());
    }

}
