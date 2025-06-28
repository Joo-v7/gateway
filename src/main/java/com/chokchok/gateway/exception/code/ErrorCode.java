package com.chokchok.gateway.exception.code;

/**
 * 에러 응답 코드를 정의하는 enum 클래스
 * 각 에러 코드에는 HTTP 상태 코드 및 고유한 코드 값이 할당
 */
public enum ErrorCode {

    // HTTP_CODE 200
    SUCCESS(2000),

    // HTTP_CODE 204
    ACCEPTED(2041),

    // HTTP_CODE 400
    // InvalidException.class
    MISSING_REQUEST_PARAMETER(4001),
    INVALID_REQUEST_PARAMETER(4002),
    INVALID_MEMBER_GENDER_VALUE(4003),
    INVALID_MEMBER_STATUS_VALUE(4004),

    INVALID_HEADER_REQUEST(4005),
    INVALID_REQUEST_TOKEN(4006),
    INVALID_ACCESS_TOKEN_REQUEST(4007),
    INVALID_REFRESH_TOKEN_REQUEST(4008),
    INVALID_LOGIN_REQUEST(4009),

    INVALID_X_MEMBER_ID_HEADER(40010),
    INVALID_X_MEMBER_ROLE_HEADER(40011),

    // HTTP_CODE 401 - 인증되지 않았거나 유효한 인증 정보가 부족
    UNAUTHORIZED(4011),
    INVALID_MEMBER_SESSION(4012),
    MISSING_AUTHORIZATION_HEADER(4013),
    UNAUTHORIZED_ACCESS_TOKEN(4014),
    UNAUTHORIZED_REFRESH_TOKEN(4015),


    // HTTP_CODE 403 - 접근 권한이 없음
    // AuthorizationException.class
    INSUFFICIENT_PERMISSION(4031),
    FORBIDDEN(4032),

    // HTTP_CODE 404
    // NotFoundException.class
    MEMBER_NOT_FOUND(4041),
    MEMBER_GRADE_NOT_FOUND(4042),
    MEMBER_ROLE_NOT_FOUND(4043),

    // HTTP_CODE 409 - 서버가 요청을 처리할 수 없음
    // ConflictException.class
    MEMBER_ALREADY_EXISTS(4091),
    MEMBER_NAME_ALREADY_EXISTS(4092),
    MEMBER_EMAIL_ALREADY_EXISTS(4093),

    // HTTP_CODE 500 - 서버 에러
    CHOKCHOK_API_FEIGN_ERROR(5000),
    AUTH_FEIGN_ERROR(5001),

    CHOKCHOK_API_SERVER_ERROR(5006),
    AUTH_API_SERVER_ERROR(5007),
    GATEWAY_SERVER_ERROR(5008);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ErrorCode from(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + code);
    }

}

