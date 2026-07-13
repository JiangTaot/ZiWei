package com.ziwei.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for table {@code ziwei_daxian} - daxian (大限) cycle per chart
 *
 * @author JTWORLD
 */
@Data
@TableName("ziwei_daxian")
public class ZiweiDaxianEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK to ziwei_chart.id */
    private Long chartId;

    /** Sequence order 1-12 */
    private Integer sequenceOrder;

    /** Start age (virtual age) */
    private Integer ageStart;

    /** End age (virtual age) */
    private Integer ageEnd;

    /** Start calendar year */
    private Integer calYearStart;

    /** End calendar year */
    private Integer calYearEnd;

    /** Palace type code (1-12) */
    private Integer palaceType;

    /** Direction: true=forward (顺行), false=reverse (逆行) */
    private Boolean direction;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

}
