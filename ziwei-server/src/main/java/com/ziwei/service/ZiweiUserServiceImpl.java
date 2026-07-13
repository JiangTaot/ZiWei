package com.ziwei.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziwei.dto.LoginResponseDTO;
import com.ziwei.dto.WechatLoginDTO;
import com.ziwei.entity.ZiweiUserEntity;
import com.ziwei.exception.BusinessException;
import com.ziwei.mapper.ZiweiUserMapper;
import com.ziwei.util.JwtUtil;
import com.ziwei.util.WechatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * User service implementation
 *
 * @author JTWORLD
 */
@Slf4j
@Service
public class ZiweiUserServiceImpl implements ZiweiUserService {

    @Resource
    private ZiweiUserMapper ziweiUserMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private WechatUtil wechatUtil;

    @Override
    public LoginResponseDTO login(WechatLoginDTO dto) {
        // Step 1: Exchange code for openid via WeChat API
        Map<String, String> session = wechatUtil.code2Session(dto.getCode());
        String openid = session.get("openid");

        // Step 2: Find user by openid, create if not exists
        LambdaQueryWrapper<ZiweiUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ZiweiUserEntity::getOpenId, openid);
        ZiweiUserEntity user = ziweiUserMapper.selectOne(wrapper);

        if (user == null) {
            user = new ZiweiUserEntity();
            user.setOpenId(openid);
            user.setNickname(dto.getNickname() != null ? dto.getNickname() : "紫微星人");
            user.setAvatarUrl(dto.getAvatar());
            user.setGender(0);
            ziweiUserMapper.insert(user);
            log.info("New user registered: id={}, openid={}", user.getId(), openid);
        } else {
            // Update nickname and avatar if provided
            boolean needUpdate = false;
            if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
                user.setNickname(dto.getNickname());
                needUpdate = true;
            }
            if (dto.getAvatar() != null && !dto.getAvatar().isEmpty()) {
                user.setAvatarUrl(dto.getAvatar());
                needUpdate = true;
            }
            if (needUpdate) {
                ziweiUserMapper.updateById(user);
            }
            log.info("User logged in: id={}, openid={}", user.getId(), openid);
        }

        // Step 3: Generate JWT token
        String token = jwtUtil.generateToken(openid, user.getId());

        // Step 4: Build response
        return LoginResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .openid(openid)
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .build();
    }

    @Override
    public ZiweiUserEntity getById(Long id) {
        ZiweiUserEntity user = ziweiUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "User not found: " + id);
        }
        return user;
    }

    @Override
    public void updateProfile(Long id, String nickname, String avatar) {
        ZiweiUserEntity user = ziweiUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "User not found: " + id);
        }
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatarUrl(avatar);
        }
        ziweiUserMapper.updateById(user);
        log.info("User profile updated: id={}", id);
    }

}
