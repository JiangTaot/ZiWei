package com.ziwei.dto;

import lombok.Data;

/**
 * WeChat login request DTO
 *
 * @author JTWORLD
 */
@Data
public class WechatLoginDTO {

    /** wx.login() returned code */
    private String code;

    /** User nickname (optional, from wx.getUserProfile) */
    private String nickname;

    /** User avatar URL (optional) */
    private String avatar;

}
