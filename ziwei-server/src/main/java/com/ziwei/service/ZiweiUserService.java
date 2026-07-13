package com.ziwei.service;

import com.ziwei.dto.LoginResponseDTO;
import com.ziwei.dto.WechatLoginDTO;
import com.ziwei.entity.ZiweiUserEntity;

/**
 * User service interface
 *
 * @author JTWORLD
 */
public interface ZiweiUserService {

    /**
     * WeChat login or register
     *
     * @param dto login request with WeChat code
     * @return login response with JWT token and user info
     */
    LoginResponseDTO login(WechatLoginDTO dto);

    /**
     * Get user by ID
     *
     * @param id user ID
     * @return user entity
     */
    ZiweiUserEntity getById(Long id);

    /**
     * Update user profile (nickname and avatar)
     *
     * @param id       user ID
     * @param nickname new nickname
     * @param avatar   new avatar URL
     */
    void updateProfile(Long id, String nickname, String avatar);

}
