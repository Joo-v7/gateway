package com.chokchok.gateway.filter;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Gateway 전역에서 확인하기 위한 Logging Filter
 */
@Slf4j
@Component
public class GlobalLoggingFilter extends AbstractGatewayFilterFactory<GlobalLoggingFilter.Config> implements Ordered {

    public GlobalLoggingFilter() {
        super(Config.class);
    }

    /**
     * 설정 클래스
     */
    @Setter
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }

    /**
     * Gateway로 들어오는 모든 요청에 대해 log를 남기기 위한 filter 로직
     * @param config - 필터의 설정 클래스
     * @return GatewayFilter
     */
    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            log.info("Global Logging Filter baseMessage: {}", config.baseMessage);
            if (config.preLogger) {
                log.info("Global Logging Filter Start: request ID -> {}", request.getId());
                log.info("Global Logging Filter Start: request URI -> {}", request.getURI());
                log.info("Global Logging Filter Start: request PATH -> {}", request.getPath());
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.postLogger) {
                    log.info("Global Logging Filter End: response CODE -> {}", response.getStatusCode());
                }
            }));

        };

    }

    /**
     * 필터 우선순위 설정 메소드
     * @return int
     */
    @Override
    public int getOrder() {
        return -1;
    }

}


