package com.ziwei.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity for table {@code ziwei_chart} - main chart record
 *
 * @author JTWORLD
 */
@Data
@TableName("ziwei_chart")
public class ZiweiChartEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** User ID */
    private Long userId;

    // === Solar birth info ===
    private Integer solarYear;
    private Integer solarMonth;
    private Integer solarDay;
    private Integer solarHour;
    private Integer solarMinute;

    /** Gender: 0=female, 1=male */
    private Integer gender;

    /** Birth place */
    private String birthPlace;

    /** Daylight saving time */
    private Boolean isDst;

    // === Lunar info ===
    private Integer lunarYear;
    private Integer lunarMonth;
    private Integer lunarDay;
    private Boolean isLeapMonth;

    // === BaZi pillars ===
    private String yearPillar;
    private String monthPillar;
    private String dayPillar;
    private String hourPillar;

    // === Chart core ===
    private String mingGongDizhi;
    private String shenGongDizhi;
    private String wuxingJu;

    /** Full chart JSON backup (serialized ChartContext) */
    private String chartJson;

    /** Create time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** Update time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** Soft delete flag */
    @TableLogic
    private Integer deleted;

}
