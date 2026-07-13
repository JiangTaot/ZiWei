package com.ziwei.controller;

import com.ziwei.dto.LoginResponseDTO;
import com.ziwei.dto.Result;
import com.ziwei.dto.WechatLoginDTO;
import com.ziwei.entity.ZiweiUserEntity;
import com.ziwei.service.ZiweiUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * User controller - WeChat login and profile management
 *
 * @author JTWORLD
 */
@RestController
@RequestMapping("/api/ziwei/user")
@Tag(name = "用户 - 微信登录")
@Slf4j
public class ZiweiUserController {

    @Resource
    private ZiweiUserService ziweiUserService;

    /**
     * WeChat login
     *
     * @param dto login request with WeChat code
     * @return JWT token and user info
     */
    @PostMapping("/login")
    @Operation(summary = "微信登录")
    public Result<LoginResponseDTO> login(@RequestBody WechatLoginDTO dto) {
        log.info("WeChat login request: code={}", dto.getCode() != null ? "***" : "null");
        LoginResponseDTO response = ziweiUserService.login(dto);
        return Result.success(response);
    }

    /**
     * Get current user profile
     *
     * @param request HTTP request with userId attribute set by JWT interceptor
     * @return user entity
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户信息")
    public Result<ZiweiUserEntity> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("Get profile: userId={}", userId);
        ZiweiUserEntity user = ziweiUserService.getById(userId);
        return Result.success(user);
    }

    /**
     * Update current user profile
     *
     * @param request HTTP request with userId attribute
     * @param body    map with nickname and/or avatar
     * @return empty success
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateProfile(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        String nickname = body.get("nickname");
        String avatar = body.get("avatar");
        log.info("Update profile: userId={}, nickname={}", userId, nickname);
        ziweiUserService.updateProfile(userId, nickname, avatar);
        return Result.success(null);
    }

}
