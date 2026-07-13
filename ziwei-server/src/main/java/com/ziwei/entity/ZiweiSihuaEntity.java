package com.ziwei.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for table {@code ziwei_sihua} - sihua (四化) record per chart
 *
 * @author JTWORLD
 */
@Data
@TableName("ziwei_sihua")
public class ZiweiSihuaEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** FK to ziwei_chart.id */
    private Long chartId;

    /** Sihua scope: 1=natal (本命), 2=daxian (大限), 3=liunian (流年) */
    private Integer sihuaScope;

    /** Reference year (null for natal) */
    private Integer refYear;

    /** Star code that transforms to 化禄 */
    private String huaLuStarCode;

    /** Star code that transforms to 化权 */
    private String huaQuanStarCode;

    /** Star code that transforms to 化科 */
    private String huaKeStarCode;

    /** Star code that transforms to 化忌 */
    private String huaJiStarCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

}
