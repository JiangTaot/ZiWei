package com.ziwei.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for table {@code ziwei_palace} - palace info per chart
 *
 * @author JTWORLD
 */
@Data
@TableName("ziwei_palace")
public class ZiweiPalaceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK to ziwei_chart.id */
    private Long chartId;

    /** Palace type code (1-12) */
    private Integer palaceType;

    /** DiZhi name (e.g. "子") */
    private String dizhi;

    /** TianGan name (e.g. "甲") */
    private String tianGan;

    /** Whether this is the body palace (身宫) */
    private Boolean isShenGong;

    /** DaXian start age */
    private Integer daXianStartAge;

    /** DaXian end age */
    private Integer daXianEndAge;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

}
