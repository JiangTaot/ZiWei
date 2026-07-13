package com.ziwei.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for table {@code ziwei_star_position} - star position per palace
 *
 * @author JTWORLD
 */
@Data
@TableName("ziwei_star_position")
public class ZiweiStarPositionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK to ziwei_chart.id */
    private Long chartId;

    /** FK to ziwei_palace.id */
    private Long palaceId;

    /** Star code (English identifier) */
    private String starCode;

    /** Star type code (1=MAJOR, 2=AUXILIARY, 3=MINOR, 4=TRANSFORM) */
    private Integer starType;

    /** Brightness code (1-7) */
    private Integer brightness;

    /** Sihua type code (1=化禄, 2=化权, 3=化科, 4=化忌) */
    private Integer sihuaType;

    /** Sort order within the palace */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

}
