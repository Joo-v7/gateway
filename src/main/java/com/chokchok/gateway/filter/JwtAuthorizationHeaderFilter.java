package com.chokchok.gateway.filter;

import com.chokchok.gateway.exception.code.ErrorCode;
import com.chokchok.gateway.exception.base.UnauthorizedException;
import com.chokchok.gateway.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Authorization 헤더의 JWT를 검증하고 파싱 후, 사용자 정보를 Request 헤더에 추가하는 필터
 */
@Slf4j
@Component
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config> {

    private final JwtUtils jwtUtils;
    private final WebClient.Builder webClient;

    public static class Config {}

    public JwtAuthorizationHeaderFilter(JwtUtils jwtUtils, WebClient.Builder webClient) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.webClient = webClient;
    }

    /**
     * 필터를 거치지 않는 경로 리스트(로그인이 필요없는 경로 리스트)
     */
    @Value("${jwt.whitelist}")
    private List<String> whitelist;

    /**
     * JWT 검증 필터
     * @param config - 설정 클래스
     * @return GatewayFilter
     */
    @Override
    public GatewayFilter apply(Config config) {
        return  (exchange, chain)->{
            ServerHttpRequest request = exchange.getRequest();

            // 화이트 리스트에 포함된 경로는 필터 통과
            log.info(request.getMethod() + " " + request.getURI());
            if (whitelist.contains(request.getURI().getPath())) {
                return chain.filter(exchange);
            }

            // header 검증
            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new UnauthorizedException(ErrorCode.MISSING_AUTHORIZATION_HEADER, "Authorization 헤더가 없습니다.");
            }else {
                // accessToken 검증
                String accessToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION).substring(7);
                if (!jwtUtils.isValidToken(accessToken)) {
                    throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_ACCESS_TOKEN, "유효하지 않은 accessToken 입니다.");
                }

                // 블랙리스트 확인
                return webClient.build()
                        .get()
                        .uri("lb://AUTH-API/auth/blacklist/{accessToken}", accessToken)
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .flatMap(isBlacklisted -> {
                            if (isBlacklisted) {
                                return Mono.error(new UnauthorizedException(ErrorCode.UNAUTHORIZED_ACCESS_TOKEN, "블랙리스트에 등록된 사용자입니다."));
                            }

                            // Request 헤더에 X-MEMBER-ID, X-MEMBER-ROLE 등록
                            String memberId = jwtUtils.extractMemberId(accessToken);
                            List<String> memberRoles = jwtUtils.extractRoles(accessToken);

                            ServerWebExchange mutedExchange = exchange.mutate()
                                    .request(builder -> builder
                                            .header("X-MEMBER-ID", memberId)
                                            .header("X-MEMBER-ROLES", String.join(",", memberRoles)))
                                    .build();

                            log.info("Request Headers: {}", mutedExchange.getRequest().getHeaders());

                            return chain.filter(mutedExchange);
                        });
            }
        };
    }
}
