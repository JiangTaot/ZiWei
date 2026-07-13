package com.ziwei.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT utility for token generation and validation
 *
 * @author JTWORLD
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * Generate a JWT token for the given user
     *
     * @param openid WeChat openid
     * @param userId user ID in database
     * @return JWT token string
     */
    public String generateToken(String openid, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(openid)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate and parse a JWT token
     *
     * @param token JWT token string
     * @return Claims object if valid
     * @throws JwtException if token is invalid or expired
     */
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract openid from JWT token
     *
     * @param token JWT token string
     * @return openid
     */
    public String getOpenidFromToken(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extract userId from JWT token
     *
     * @param token JWT token string
     * @return userId
     */
    public Long getUserIdFromToken(String token) {
        Number userId = validateToken(token).get("userId", Number.class);
        return userId != null ? userId.longValue() : null;
    }

}
