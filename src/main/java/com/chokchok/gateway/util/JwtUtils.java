package com.chokchok.gateway.util;

import com.chokchok.gateway.exception.base.InvalidException;
import com.chokchok.gateway.exception.code.ErrorCode;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 검증, 파싱을 위한 클래스
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String JWT_SECRET_KEY;

    /**
     * JWT를 파싱하기 위해 HMAC-SHA256 알고리즘으로 키를 생성
     *
     * @return SecretKey - 대칭키 방식이므로 secretkey 반환
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * HMAC-SHA 알고리즘으로 생성한 secretKey를 기반으로 JWT 토큰의 유효성 검사하는 메소드
     *
     * @param token
     * @return boolean - 토큰의 유효성 판별 결과
     */
    public boolean isValidToken(String token) {
        try {

            Jwts.parser().verifyWith(getSecretKey()).build();
            return true;

        } catch(JwtException e) {
            return false;
        }
    }

    /**
     * JWT 토큰을 파싱하여 payload에 들어있는 회원의 id을 반환하는 메소드
     *
     * @param token
     * @return String - 회원 id
     */
    public String extractMemberId(String token) {
        try {

            Object id = Jwts.parser()
                    .verifyWith(getSecretKey()).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("id");

            return String.valueOf(id);

        } catch(JwtException e) {
            throw new InvalidException(ErrorCode.INVALID_REQUEST_TOKEN, "유효하지 않은 JWT 토큰");
        }
    }

    /**
     * JWT 토큰을 파싱하여 payload에 들어있는 회원의 roles를 반환하는 메소드
     * @param token - JWT
     * @return List<String> - roles
     */
    public List<String> extractRoles(String token) {
        try {

            Object roles = Jwts.parser()
                    .verifyWith(getSecretKey()).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("roles");

            return List.of(roles.toString());

        } catch(JwtException e) {
            throw new InvalidException(ErrorCode.INVALID_REQUEST_TOKEN, "유효하지 않은 JWT 토큰");
        }
    }

}
