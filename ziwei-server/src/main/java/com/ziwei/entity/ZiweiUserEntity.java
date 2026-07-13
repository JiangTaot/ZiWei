package com.ziwei.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for table {@code ziwei_user} - user profile
 *
 * @author JTWORLD
 */
@Data
@TableName("ziwei_user")
public class ZiweiUserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** WeChat openId (nullable, for future auth) */
    private String openId;

    /** Nickname */
    private String nickname;

    /** Avatar URL */
    @TableField("avatar")
    private String avatarUrl;

    /** Gender: 0=female, 1=male */
    private Integer gender;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

}
