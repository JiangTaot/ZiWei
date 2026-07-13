package com.ziwei.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response DTO with JWT token and user info
 *
 * @author JTWORLD
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    /** JWT token */
    private String token;

    /** User ID */
    private Long userId;

    /** WeChat openid */
    private String openid;

    /** User nickname */
    private String nickname;

    /** User avatar URL */
    private String avatar;

}
