package com.campus.delivery.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发与解析 Token。
 *
 * <p>Token 中只承载身份标识与角色，不放业务数据，避免 Token 体积膨胀。
 * 密钥通过环境变量 {@code JWT_SECRET} 注入，禁止硬编码到仓库。
 */
@Slf4j
@Component
public class JwtUtils {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_NICKNAME = "nickname";

    @Value("${campus.jwt.secret}")
    private String secret;

    @Value("${campus.jwt.expire-minutes:720}")
    private long expireMinutes;

    private SecretKey signKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发 Token */
    public String generateToken(LoginUser user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireMinutes * 60_000L);
        return Jwts.builder()
                .subject(user.getUserId())
                .claim(CLAIM_USERNAME, user.getUsername())
                .claim(CLAIM_ROLE, user.getRole())
                .claim(CLAIM_NICKNAME, user.getNickname())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signKey())
                .compact();
    }

    /** 解析 Token，失败返回 null（由调用方决定抛 401 还是放行） */
    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new LoginUser(
                    claims.getSubject(),
                    claims.get(CLAIM_USERNAME, String.class),
                    claims.get(CLAIM_ROLE, String.class),
                    claims.get(CLAIM_NICKNAME, String.class));
        } catch (Exception e) {
            log.debug("Token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /** Token 有效期（分钟） */
    public long getExpireMinutes() {
        return expireMinutes;
    }
}
