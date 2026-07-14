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
 * JWT 认证拦截器
 * <p>
 * 从 Authorization header 提取 Bearer token，校验后注入 userId/openid 到 request attributes。
 * 对 GET /api/ziwei/chart/{id}（数字 ID）做可选认证：有 token 则提取 userId，无 token 也放行。
 * 其余 /api/** 路径强制要求有效 token。
 *
 * @author JTWORLD
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /** request attribute key：当前用户 ID */
    public static final String USER_ID_ATTR = "userId";

    /** request attribute key：当前用户 openid */
    public static final String OPENID_ATTR = "openid";

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // GET /api/ziwei/chart/{数字ID} — 可选认证（匿名用户也能查看命盘详情）
        if ("GET".equalsIgnoreCase(request.getMethod()) && path.matches(".*/api/ziwei/chart/\\d+$")) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String openid = jwtUtil.getOpenidFromToken(token);
                    request.setAttribute(USER_ID_ATTR, userId);
                    request.setAttribute(OPENID_ATTR, openid);
                    log.debug("JWT 可选认证: userId={}, openid={}", userId, openid);
                } catch (JwtException e) {
                    log.debug("JWT 可选认证: token 无效，放行匿名请求");
                }
            }
            return true;
        }

        // 其余路径 — 强制认证
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
            } catch (Exception ex) {
                log.warn("写入 401 响应失败", ex);
            }
            return false;
        }

        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String openid = jwtUtil.getOpenidFromToken(token);
            request.setAttribute(USER_ID_ATTR, userId);
            request.setAttribute(OPENID_ATTR, openid);
            log.debug("JWT 认证成功: userId={}, openid={}", userId, openid);
            return true;
        } catch (JwtException e) {
            log.warn("JWT 校验失败: {}", e.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"msg\":\"token 无效或已过期\",\"data\":null}");
            } catch (Exception ex) {
                log.warn("写入 401 响应失败", ex);
            }
            return false;
        }
    }

}
