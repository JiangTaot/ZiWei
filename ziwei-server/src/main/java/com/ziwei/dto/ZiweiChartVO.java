package com.ziwei.dto;

import com.ziwei.engine.pattern.PatternResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Full chart response VO
 *
 * @author JTWORLD
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZiweiChartVO {

    /** Chart ID in database */
    private Long id;

    /** Owner user ID */
    private Long userId;

    // ========== Birth Info ==========
    private int solarYear;
    private int solarMonth;
    private int solarDay;
    private int solarHour;
    private int solarMinute;
    private int gender;
    private String birthPlace;
    private boolean isDst;

    // ========== Lunar Info ==========
    private int lunarYear;
    private int lunarMonth;
    private int lunarDay;
    private boolean isLeapMonth;

    // ========== BaZi (Four Pillars) ==========
    private String yearPillar;
    private String monthPillar;
    private String dayPillar;
    private String hourPillar;

    // ========== Chart Core ==========
    /** Ming Gong (命宫) DiZhi name + "宫" */
    private String mingGong;

    /** Shen Gong (身宫) DiZhi name + "宫" */
    private String shenGong;

    /** WuXingJu name (e.g. "水二局") */
    private String wuxingJu;

    // ========== Palaces ==========
    /** Twelve palaces */
    @Builder.Default
    private List<ZiweiPalaceVO> palaces = new ArrayList<>();

    // ========== Natal Sihua ==========
    private NatalSihuaVO natalSihua;

    // ========== DaXian ==========
    /** DaXian cycles */
    @Builder.Default
    private List<DaxianVO> daxians = new ArrayList<>();

    // ========== Patterns ==========
    /** Pattern names */
    @Builder.Default
    private List<String> patterns = new ArrayList<>();

    /** Pattern details */
    @Builder.Default
    private List<PatternDetailVO> patternDetails = new ArrayList<>();

    // ========== Auditing ==========
    private LocalDateTime createTime;

    // ========== Inner VOs ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NatalSihuaVO {
        private ZiweiStarVO huaLu;
        private ZiweiStarVO huaQuan;
        private ZiweiStarVO huaKe;
        private ZiweiStarVO huaJi;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaxianVO {
        private int ageStart;
        private int ageEnd;
        private String palaceName;
        private String direction;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatternDetailVO {
        private String name;
        private String level;
        private String description;
        private String source;
        private List<String> required;
        private List<String> bonus;
        private List<String> breaking;
    }

}
