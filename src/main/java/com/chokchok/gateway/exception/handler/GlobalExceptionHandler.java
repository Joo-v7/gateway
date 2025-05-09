package com.chokchok.gateway.exception.handler;

import com.chokchok.gateway.exception.base.InvalidException;
import com.chokchok.gateway.exception.base.UnauthorizedException;
import com.chokchok.gateway.exception.code.ErrorCode;
import com.chokchok.gateway.exception.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.NonNullApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.Hints;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 게이트웨이 내부에서 발생하는 예외에 대한 전체적인 핸들링을 담당하는 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Component
@NonNullApi
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type", "application/json");

        ErrorResponseDto errorResponseDto;

        // UnauthorizedException 에러 처리
        if(ex instanceof UnauthorizedException unauthorizedException) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);

            errorResponseDto = ErrorResponseDto.of(
                    HttpStatus.UNAUTHORIZED.value(),
                    unauthorizedException.getErrorCode().getCode(),
                    ex.getMessage()
            );

            // InvalidException 에러 처리
        } else if(ex instanceof InvalidException invalidException) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);

            errorResponseDto = ErrorResponseDto.of(
                    HttpStatus.BAD_REQUEST.value(),
                    invalidException.getErrorCode().getCode(),
                    ex.getMessage()
            );

            // 그 외 에러 처리
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

            errorResponseDto = ErrorResponseDto.of(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    ErrorCode.GATEWAY_SERVER_ERROR.getCode(),
                    ex.getMessage()
            );
        }

        return response.writeWith(
                new Jackson2JsonEncoder(objectMapper)
                        .encode(
                                Mono.just(errorResponseDto),
                                response.bufferFactory(),
                                ResolvableType.forInstance(errorResponseDto),
                                MediaType.APPLICATION_JSON,
                                Hints.from(Hints.LOG_PREFIX_HINT, exchange.getLogPrefix())
                        )
        );

    }

}
