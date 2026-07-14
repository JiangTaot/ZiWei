package com.ziwei.config;

import com.ziwei.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.Resource;
import java.io.IOException;

/**
 * JWT 可选提取过滤器
 * <p>
 * 在所有 /api/** 请求上先于 JwtInterceptor 执行。
 * 如果请求携带有效的 Bearer token，从中提取 userId 和 openid 并写入 request attributes。
 * 如果 token 缺失或无效，请求继续放行（不阻断），由下游 JwtInterceptor 对非白名单路径强制认证。
 * <p>
 * 这样白名单端点（如 /chart/calculate）可以在用户已登录时关联 userId，
 * 同时未登录用户仍可正常使用排盘功能。
 *
 * @author JTWORLD
 */
@Slf4j
@Component
public class OptionalJwtFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String openid = jwtUtil.getOpenidFromToken(token);
                request.setAttribute(JwtInterceptor.USER_ID_ATTR, userId);
                request.setAttribute(JwtInterceptor.OPENID_ATTR, openid);
                log.debug("OptionalJwtFilter: 已提取 userId={}, openid={}", userId, openid);
            } catch (JwtException e) {
                // token 无效 — 不阻断，仅不设置 userId
                log.debug("OptionalJwtFilter: token 无效，放行匿名请求");
            }
        }
        filterChain.doFilter(request, response);
    }

}
