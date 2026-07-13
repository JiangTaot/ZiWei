package com.ziwei.config;

import com.ziwei.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;

/**
 * JWT authentication interceptor
 * <p>
 * Extracts Bearer token from Authorization header, validates it,
 * and injects userId into request attributes for downstream controllers.
 *
 * @author JTWORLD
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /** Request attribute key for current userId */
    public static final String USER_ID_ATTR = "userId";

    /** Request attribute key for current openid */
    public static final String OPENID_ATTR = "openid";

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Allow OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"msg\":\"Missing or invalid Authorization header\",\"data\":null}");
            } catch (Exception ex) {
                log.warn("Failed to write 401 response", ex);
            }
            return false;
        }

        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String openid = jwtUtil.getOpenidFromToken(token);
            request.setAttribute(USER_ID_ATTR, userId);
            request.setAttribute(OPENID_ATTR, openid);
            log.debug("JWT authenticated: userId={}, openid={}", userId, openid);
            return true;
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"msg\":\"Invalid or expired token\",\"data\":null}");
            } catch (Exception ex) {
                log.warn("Failed to write 401 response", ex);
            }
            return false;
        }
    }

}
