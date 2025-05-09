package com.chokchok.gateway.filter;

import com.chokchok.gateway.client.AuthClient;
import com.chokchok.gateway.exception.code.ErrorCode;
import com.chokchok.gateway.exception.base.UnauthorizedException;
import com.chokchok.gateway.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Authorization 헤더의 JWT를 검증하고 파싱 후, 사용자 정보를 Request 헤더에 추가하는 필터
 */
@Slf4j
@Component
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config> {

    private final JwtUtils jwtUtils;

    private final AuthClient authClient;

    public static class Config {}

    public JwtAuthorizationHeaderFilter(JwtUtils jwtUtils, @Lazy AuthClient authClient) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.authClient = authClient;
    }

    /**
     * 필터를 거치지 않는 경로 리스트(로그인이 필요없는 경로 리스트)
     */
    private static final List<String> WHITELIST = List.of(
            // Auth
            "/auth/login"

            // API
    );

    /**
     * JWT 검증 필터
     * @param config - 설정 클래스
     * @return GatewayFilter
     */
    @Override
    public GatewayFilter apply(Config config) {
        return  (exchange, chain)->{

            ServerHttpRequest request = exchange.getRequest();
            String accessToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 화이트 리스트에 포함된 경로면 필터 통과
            if (WHITELIST.contains(request.getURI().getPath())) {
                return chain.filter(exchange);
            }

            // header 검증
            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new UnauthorizedException(ErrorCode.MISSING_AUTHORIZATION_HEADER, "Authorization 헤더가 없습니다.");
            }else{
                // accessToken 검증, 블랙리스트 체크
                if(!jwtUtils.isValidToken(accessToken) || authClient.isTokenBlacklisted(accessToken)) {
                    throw new UnauthorizedException(ErrorCode.UNAUTHORIZED_ACCESS_TOKEN, "유효하지 않은 accessToken 입니다.");
                }

                // Request 헤더에 X-MEMBER-ID 등록
                String memberId = jwtUtils.extractMemberId(accessToken);
                exchange.mutate().request(builder -> {
                    builder.header("X-MEMBER-ID",memberId);
                });

                // Request 헤더에 X-MEMBER-ROLE 등록
                List<String> memberRoles = jwtUtils.extractRoles(accessToken);
                exchange.mutate().request(builder -> {
                    builder.header("X-MEMBER-ROLES", String.join(",", memberRoles));
                });
            }

            return chain.filter(exchange);
        };
    }

}
