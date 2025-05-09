package com.chokchok.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * auth-api와 통신하는 Feign Client
 */
@FeignClient(name = "AUTH-API")
public interface AuthClient {

    @GetMapping("/auth/blacklist/{accessToken}")
    boolean isTokenBlacklisted(@PathVariable String accessToken);

}
